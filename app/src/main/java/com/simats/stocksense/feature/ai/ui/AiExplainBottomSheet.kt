package com.simats.stocksense.feature.ai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simats.stocksense.R
import com.simats.stocksense.data.repository.AiRepository
import kotlinx.coroutines.launch

private val DarkNavyBg = Color(0xFF0F172A)
private val CardNavyBg = Color(0xFF1E293B)
private val VibrantGreen = Color(0xFF34D399)
private val DarkGreenText = Color(0xFF064E3B)
private val MutedText = Color(0xFF94A3B8)

@Composable
fun AiExplainBottomSheet(
    type: String,
    productId: Int,
    productName: String = "",
    onDismiss: () -> Unit,
    repository: AiRepository = remember { AiRepository() }
) {
    var isLoading by remember { mutableStateOf(true) }
    var explanationText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(type, productId) {
        isLoading = true
        errorMessage = null
        val result = repository.explain(type = type, productId = productId)
        result.onSuccess { text ->
            explanationText = text
            isLoading = false
        }.onFailure {
            errorMessage = "Could not explain right now."
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() },
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

                // Header with Dukaan Mitra Avatar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.dukaan_mitra),
                        contentDescription = "Dukaan Mitra",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, VibrantGreen, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Why this recommendation?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        if (productName.isNotBlank()) {
                            Text(
                                text = productName,
                                color = MutedText,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Content area
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = VibrantGreen,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Analyzing your shop data...",
                                    color = MutedText,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardNavyBg)
                                .padding(18.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "Could not explain right now.",
                                color = Color(0xFFFCA5A5),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    explanationText != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardNavyBg)
                                .padding(18.dp)
                        ) {
                            Text(
                                text = explanationText ?: "",
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen)
                ) {
                    Text(
                        text = "Got it",
                        color = DarkGreenText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

