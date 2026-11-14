package com.simats.stocksense.feature.ai.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.R
import com.simats.stocksense.data.model.AiAction
import com.simats.stocksense.data.model.AiProductCard
import com.simats.stocksense.data.model.ChatMessage
import com.simats.stocksense.feature.ai.viewmodel.AiChatViewModel
import java.util.*

private val DarkNavyBg = Color(0xFF0F172A)
private val CardNavyBg = Color(0xFF1E293B)
private val CardBorderColor = Color(0xFF334155)
private val VibrantGreen = Color(0xFF34D399)
private val DarkGreenText = Color(0xFF064E3B)
private val UserBubbleColor = Color(0xFF34D399)
private val MutedText = Color(0xFF94A3B8)
private val LowChipBg = Color(0xFFFEF3C7)
private val LowChipText = Color(0xFFD97706)

@Composable
fun AiChatScreen(
    onBackClick: () -> Unit = {},
    onNavigate: (String, Int?) -> Unit = { _, _ -> },
    onSessionExpired: () -> Unit = {},
    viewModel: AiChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Session expiration handling
    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onSessionExpired()
        }
    }

    // --- Text To Speech Engine ---
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsInitialized by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInitialized = true
            }
        }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                utteranceId?.let { viewModel.setSpeakingMessageId(it) }
            }
            override fun onDone(utteranceId: String?) {
                viewModel.setSpeakingMessageId(null)
            }
            override fun onError(utteranceId: String?) {
                viewModel.setSpeakingMessageId(null)
            }
        })
        ttsEngine = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun stopSpeaking() {
        ttsEngine?.stop()
        viewModel.setSpeakingMessageId(null)
    }

    fun speakText(messageId: String, text: String) {
        val tts = ttsEngine ?: return
        if (!ttsInitialized) {
            Toast.makeText(context, "Voice engine initializing...", Toast.LENGTH_SHORT).show()
            return
        }

        if (uiState.speakingMessageId == messageId) {
            stopSpeaking()
            return
        }

        stopSpeaking()

        val locale = detectLanguage(text)
        val available = tts.isLanguageAvailable(locale)
        if (available == TextToSpeech.LANG_MISSING_DATA || available == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(context, "Voice not available for this language", Toast.LENGTH_SHORT).show()
            return
        }

        val setResult = tts.setLanguage(locale)
        if (setResult == TextToSpeech.LANG_MISSING_DATA || setResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(context, "Voice not available for this language", Toast.LENGTH_SHORT).show()
            return
        }

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, messageId)
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, messageId)
    }

    // Auto-speak answer if enabled
    val lastMessage = uiState.messages.lastOrNull()
    LaunchedEffect(lastMessage?.id) {
        if (lastMessage != null && !lastMessage.isUser && !lastMessage.isError && uiState.autoSpeak) {
            speakText(lastMessage.id, lastMessage.text)
        }
    }

    // --- Speech Recognizer Setup ---
    var showPermissionRationale by remember { mutableStateOf(false) }
    var permissionDeniedPermanently by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            stopSpeaking()
            viewModel.showListeningSheet()
        } else {
            permissionDeniedPermanently = true
            Toast.makeText(context, "Microphone permission is required for voice input.", Toast.LENGTH_SHORT).show()
        }
    }

    fun onMicTapped() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            stopSpeaking()
            viewModel.showListeningSheet()
        } else {
            if (permissionDeniedPermanently) {
                Toast.makeText(context, "Microphone access denied. Enable it in App Settings to use voice.", Toast.LENGTH_SHORT).show()
            } else {
                showPermissionRationale = true
            }
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = {
                Text(
                    text = "Microphone Access",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Dukaan Mitra uses your microphone to transcribe your voice questions in English, Tamil, or Hindi.",
                    color = MutedText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                ) {
                    Text("Allow", color = VibrantGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Cancel", color = MutedText)
                }
            },
            containerColor = CardNavyBg
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkNavyBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Header
            AiChatHeader(
                autoSpeak = uiState.autoSpeak,
                onBackClick = onBackClick,
                onAutoSpeakToggle = { viewModel.setAutoSpeak(it) }
            )

            // 2. Chat Body (Welcome State or Message List)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.messages.isEmpty()) {
                    WelcomeContent(
                        onQuestionClick = { q -> viewModel.sendMessage(q) }
                    )
                } else {
                    ConversationContent(
                        messages = uiState.messages,
                        isTyping = uiState.isTyping,
                        showStillThinking = uiState.showStillThinking,
                        speakingMessageId = uiState.speakingMessageId,
                        onSpeakClick = { id, text -> speakText(id, text) },
                        onActionClick = { act -> onNavigate(act.target, act.productId) },
                        onSuggestionClick = { msg -> viewModel.sendMessage(msg) },
                        onRetryClick = { q -> viewModel.retryQuestion(q) }
                    )
                }
            }

            // 3. Bottom Input Bar with Soft Keyboard Padding
            InputBar(
                text = uiState.inputText,
                isTyping = uiState.isTyping,
                onTextChange = { viewModel.onInputTextChanged(it) },
                onSend = { viewModel.sendMessage(uiState.inputText) },
                onMicClick = { onMicTapped() }
            )
        }
    }

    // 4. Listening Bottom Sheet
    if (uiState.isListeningSheetVisible) {
        ListeningSheet(
            selectedLanguage = uiState.selectedLanguage,
            partialTranscript = uiState.partialTranscript,
            onLanguageSelected = { viewModel.setSelectedLanguage(it) },
            onPartialTranscript = { viewModel.updatePartialTranscript(it) },
            onCancel = { viewModel.hideListeningSheet() },
            onSend = { text -> viewModel.onSpeechSend(text) }
        )
    }
}

// -----------------------------------------------------------------------------
// HEADER
// -----------------------------------------------------------------------------
@Composable
private fun AiChatHeader(
    autoSpeak: Boolean,
    onBackClick: () -> Unit,
    onAutoSpeakToggle: (Boolean) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardNavyBg)
                .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.65f, size.height * 0.15f)
                    lineTo(size.width * 0.25f, size.height * 0.5f)
                    lineTo(size.width * 0.65f, size.height * 0.85f)
                }
                drawPath(
                    path = path,
                    color = VibrantGreen,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Avatar (52dp)
        Image(
            painter = painterResource(id = R.drawable.dukaan_mitra),
            contentDescription = "Dukaan Mitra",
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .border(1.5.dp, VibrantGreen, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Dukaan Mitra",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Your shop assistant",
                color = MutedText,
                fontSize = 12.sp
            )
        }

        // Overflow Menu with Auto-speak toggle
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(CardNavyBg)
            ) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Auto-speak answers", color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = autoSpeak,
                                onCheckedChange = {
                                    onAutoSpeakToggle(it)
                                    showMenu = false
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = VibrantGreen
                                )
                            )
                        }
                    },
                    onClick = {
                        onAutoSpeakToggle(!autoSpeak)
                        showMenu = false
                    }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// WELCOME STATE (Design Screen 1)
// -----------------------------------------------------------------------------
@Composable
private fun WelcomeContent(
    onQuestionClick: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Large Animated Avatar (120dp) with soft glow & floating
        Box(
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .size(136.dp)
                .background(VibrantGreen.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.dukaan_mitra),
                contentDescription = "Dukaan Mitra Avatar",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, VibrantGreen, CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Namaste! I'm Dukaan Mitra",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your shop friend who never sleeps",
            color = MutedText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Welcome Speech Bubble
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.dukaan_mitra),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, VibrantGreen, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                    .background(CardNavyBg)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ask me about your stock and sales, in any language. I'll do the maths.",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Try Asking Section
        Text(
            text = "TRY ASKING",
            color = MutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        val quickQuestions = listOf(
            "What should I reorder this week?",
            "Which items are low on stock?",
            "How much did I sell today?",
            "Which products are not selling?"
        )

        quickQuestions.forEach { question ->
            QuickQuestionCard(
                text = question,
                onClick = { onQuestionClick(question) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuickQuestionCard(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardNavyBg)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Canvas(modifier = Modifier.size(14.dp)) {
            val path = Path().apply {
                moveTo(size.width * 0.35f, size.height * 0.15f)
                lineTo(size.width * 0.75f, size.height * 0.5f)
                lineTo(size.width * 0.35f, size.height * 0.85f)
            }
            drawPath(
                path = path,
                color = VibrantGreen,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// CONVERSATION CONTENT (Design Screen 2)
// -----------------------------------------------------------------------------
@Composable
private fun ConversationContent(
    messages: List<ChatMessage>,
    isTyping: Boolean,
    showStillThinking: Boolean,
    speakingMessageId: String?,
    onSpeakClick: (String, String) -> Unit,
    onActionClick: (AiAction) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onRetryClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isTyping) {
        val count = messages.size + (if (isTyping) 1 else 0)
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(messages, key = { it.id }) { msg ->
            if (msg.isUser) {
                UserMessageRow(message = msg)
            } else {
                AssistantMessageRow(
                    message = msg,
                    isSpeaking = speakingMessageId == msg.id,
                    onSpeakClick = { onSpeakClick(msg.id, msg.text) },
                    onActionClick = onActionClick,
                    onSuggestionClick = onSuggestionClick,
                    onRetryClick = onRetryClick
                )
            }
        }

        if (isTyping) {
            item(key = "typing_indicator") {
                TypingIndicatorRow(showStillThinking = showStillThinking)
            }
        }
    }
}

@Composable
private fun UserMessageRow(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                .background(UserBubbleColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                color = DarkGreenText,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun AssistantMessageRow(
    message: ChatMessage,
    isSpeaking: Boolean,
    onSpeakClick: () -> Unit,
    onActionClick: (AiAction) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onRetryClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.dukaan_mitra),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, VibrantGreen, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Text Bubble with Speaker icon
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                    .background(CardNavyBg)
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = message.text,
                            color = if (message.isError) Color(0xFFFCA5A5) else Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )

                        if (!message.isError) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { onSpeakClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    val iconColor = if (isSpeaking) VibrantGreen else MutedText
                                    // Speaker cone
                                    val path = Path().apply {
                                        moveTo(size.width * 0.15f, size.height * 0.35f)
                                        lineTo(size.width * 0.35f, size.height * 0.35f)
                                        lineTo(size.width * 0.65f, size.height * 0.15f)
                                        lineTo(size.width * 0.65f, size.height * 0.85f)
                                        lineTo(size.width * 0.35f, size.height * 0.65f)
                                        lineTo(size.width * 0.15f, size.height * 0.65f)
                                        close()
                                    }
                                    drawPath(path, color = iconColor)

                                    // Sound waves
                                    if (isSpeaking) {
                                        drawArc(
                                            color = iconColor,
                                            startAngle = -45f,
                                            sweepAngle = 90f,
                                            useCenter = false,
                                            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (message.isError && message.retryQuestion != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onRetryClick(message.retryQuestion) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, VibrantGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Grounded Product Cards (from database records)
            if (message.cards.isNotEmpty()) {
                ProductCardsContainer(cards = message.cards)
            }

            // Action Buttons (Open screen navigation only)
            if (message.actions.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    message.actions.forEach { action ->
                        OutlinedButton(
                            onClick = { onActionClick(action) },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, VibrantGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantGreen),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = action.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantGreen
                            )
                        }
                    }
                }
            }

            // Suggestion Chips (Send chat message, NEVER navigate)
            if (message.suggestions.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    message.suggestions.forEach { suggestion ->
                        Surface(
                            onClick = { onSuggestionClick(suggestion.message) },
                            shape = RoundedCornerShape(16.dp),
                            color = CardNavyBg,
                            border = BorderStroke(1.dp, CardBorderColor)
                        ) {
                            Text(
                                text = suggestion.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCardsContainer(cards: List<AiProductCard>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardNavyBg),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column {
            cards.forEachIndexed { index, card ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProductAvatar(
                            name = card.name,
                            category = null,
                            size = 36.dp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = card.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val salesText = if (card.avgDailySales > 0) {
                                "${card.unitsLeft} units left · sells ${card.avgDailySales.toInt()} a day"
                            } else {
                                "${card.unitsLeft} units left"
                            }
                            Text(
                                text = salesText,
                                color = MutedText,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }

                    val statusText = card.status ?: "In stock"
                    val isLow = statusText.contains("Low", ignoreCase = true) || statusText.contains("Out", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLow) LowChipBg else Color(0xFFD1FAE5))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isLow) "Low" else "Stock",
                            color = if (isLow) LowChipText else DarkGreenText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (index < cards.size - 1) {
                    HorizontalDivider(color = CardBorderColor, thickness = 0.8.dp)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TYPING INDICATOR (3-dot pulsation & "Still thinking...")
// -----------------------------------------------------------------------------
@Composable
private fun TypingIndicatorRow(showStillThinking: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val avatarScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarScale"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.dukaan_mitra),
            contentDescription = null,
            modifier = Modifier
                .scale(avatarScale)
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, VibrantGreen, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardNavyBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                ThreeDotsTypingIndicator()
            }

            if (showStillThinking) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Still thinking...",
                    color = MutedText,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ThreeDotsTypingIndicator() {
    val transition = rememberInfiniteTransition(label = "dots")

    val dot1Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 0, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(7.dp).background(VibrantGreen.copy(alpha = dot1Alpha), CircleShape))
        Box(modifier = Modifier.size(7.dp).background(VibrantGreen.copy(alpha = dot2Alpha), CircleShape))
        Box(modifier = Modifier.size(7.dp).background(VibrantGreen.copy(alpha = dot3Alpha), CircleShape))
    }
}

// -----------------------------------------------------------------------------
// INPUT BAR (Floating above IME keyboard)
// -----------------------------------------------------------------------------
@Composable
private fun InputBar(
    text: String,
    isTyping: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Input Pill
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(CardNavyBg)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(26.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Ask in English, Tamil, Hindi...",
                            color = MutedText,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(VibrantGreen),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mic Button inside the field
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .clickable { onMicClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        // Microphone shape
                        val w = size.width
                        val h = size.height
                        drawRoundRect(
                            color = DarkGreenText,
                            topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.15f),
                            size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.45f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.18f, w * 0.18f)
                        )
                        drawArc(
                            color = DarkGreenText,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.28f),
                            size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.42f),
                            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawLine(
                            color = DarkGreenText,
                            start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.70f),
                            end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.88f),
                            strokeWidth = 1.6.dp.toPx()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Green Up-Arrow Send Button
            val canSend = text.isNotBlank() && !isTyping
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (canSend) VibrantGreen else VibrantGreen.copy(alpha = 0.35f))
                    .clickable(enabled = canSend) { onSend() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.5f, size.height * 0.2f)
                        lineTo(size.width * 0.2f, size.height * 0.5f)
                        moveTo(size.width * 0.5f, size.height * 0.2f)
                        lineTo(size.width * 0.8f, size.height * 0.5f)
                        moveTo(size.width * 0.5f, size.height * 0.2f)
                        lineTo(size.width * 0.5f, size.height * 0.85f)
                    }
                    drawPath(
                        path = path,
                        color = if (canSend) DarkGreenText else DarkGreenText.copy(alpha = 0.5f),
                        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "AI can make mistakes. Check important numbers.",
            color = MutedText,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

// -----------------------------------------------------------------------------
// LISTENING SHEET (Design Screen 3 - Speech Recognition)
// -----------------------------------------------------------------------------
@Composable
private fun ListeningSheet(
    selectedLanguage: String,
    partialTranscript: String,
    onLanguageSelected: (String) -> Unit,
    onPartialTranscript: (String) -> Unit,
    onCancel: () -> Unit,
    onSend: (String) -> Unit
) {
    val context = LocalContext.current
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var currentTranscript by remember { mutableStateOf(partialTranscript) }

    fun startListening(langTag: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show()
            return
        }

        speechRecognizer?.destroy()

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                // If no match found or timed out, leave current transcript
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    currentTranscript = text
                    onPartialTranscript(text)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    currentTranscript = text
                    onPartialTranscript(text)
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)
        speechRecognizer = recognizer
    }

    LaunchedEffect(selectedLanguage) {
        startListening(selectedLanguage)
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "p1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "a1"
    )

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onCancel() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(DarkNavyBg)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(MutedText.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Language Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val languages = listOf(
                        "English" to "en-IN",
                        "தமிழ்" to "ta-IN",
                        "हिन्दी" to "hi-IN"
                    )
                    languages.forEach { (label, code) ->
                        val isSelected = selectedLanguage == code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) VibrantGreen else CardNavyBg)
                                .clickable {
                                    onLanguageSelected(code)
                                }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) DarkGreenText else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Pulsing Mic
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Outer Wave
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale1)
                            .background(VibrantGreen.copy(alpha = pulseAlpha1), CircleShape)
                    )

                    // Secondary Ring
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(VibrantGreen.copy(alpha = 0.18f), CircleShape)
                    )

                    // Center Green Circle (72dp)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(VibrantGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(32.dp)) {
                            val w = size.width
                            val h = size.height
                            drawRoundRect(
                                color = DarkGreenText,
                                topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.12f),
                                size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.48f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.18f, w * 0.18f)
                            )
                            drawArc(
                                color = DarkGreenText,
                                startAngle = 0f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.26f),
                                size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.45f),
                                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawLine(
                                color = DarkGreenText,
                                start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.72f),
                                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.90f),
                                strokeWidth = 2.4.dp.toPx()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "LISTENING",
                    color = VibrantGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live Partial Transcript
                val displayText = if (currentTranscript.isNotBlank()) currentTranscript else "Speak now..."
                Text(
                    text = displayText,
                    color = if (currentTranscript.isNotBlank()) Color.White else MutedText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    minLines = 2,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Bottom Buttons: Cancel and Send
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CardNavyBg),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Button(
                        onClick = { onSend(currentTranscript) },
                        enabled = currentTranscript.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VibrantGreen,
                            disabledContainerColor = VibrantGreen.copy(alpha = 0.35f)
                        )
                    ) {
                        Text("Send", color = DarkGreenText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Detect language script from text for proper TTS voice selection.
 */
private fun detectLanguage(text: String): Locale {
    var hasTamil = false
    var hasDevanagari = false
    for (char in text) {
        val code = char.code
        if (code in 0x0B80..0x0BFF) {
            hasTamil = true
            break
        } else if (code in 0x0900..0x097F) {
            hasDevanagari = true
            break
        }
    }
    return when {
        hasTamil -> Locale.forLanguageTag("ta-IN")
        hasDevanagari -> Locale.forLanguageTag("hi-IN")
        else -> Locale.forLanguageTag("en-IN")
    }
}

