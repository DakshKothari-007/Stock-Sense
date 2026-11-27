package com.simats.stocksense.feature.reorder.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.feature.ai.ui.AiExplainBottomSheet
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.reorder.viewmodel.ReorderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReorderRecommendationScreen(
    onBackClick: () -> Unit = {},
    onAcceptClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
    onViewOrdersClick: () -> Unit = {},
    viewModel: ReorderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showWhyDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Clear target selection when leaving this screen (preserving across rotation)
    val hostActivity = context as? android.app.Activity
    DisposableEffect(Unit) {
        onDispose {
            if (hostActivity?.isChangingConfigurations != true) {
                viewModel.clearSelection()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamBackground
    ) {
        when {
            (uiState.isLoading && uiState.items.isEmpty()) || uiState.isAwaitingSelection -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MainBlue,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Checking reorder recommendations...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk
                        )
                    }
                }
            }

            uiState.errorMessage != null && uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SoftPink,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Unable to load recommendations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusDangerRed
                            )
                            Text(
                                text = uiState.errorMessage ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkDark,
                                textAlign = TextAlign.Center
                            )
                            BoldButton(
                                text = "Retry",
                                onClick = { viewModel.refresh() },
                                isPrimary = true
                            )
                        }
                    }
                }
            }

            uiState.recommendation == null || uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = CardWhite,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(SoftLightBlue, RoundedCornerShape(28.dp))
                                    .border(2.dp, InkDark, RoundedCornerShape(28.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(24.dp)) {
                                    val path = Path().apply {
                                        moveTo(size.width * 0.2f, size.height * 0.52f)
                                        lineTo(size.width * 0.42f, size.height * 0.74f)
                                        lineTo(size.width * 0.8f, size.height * 0.26f)
                                    }
                                    drawPath(
                                        path = path,
                                        color = InkDark,
                                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }
                            Text(
                                text = "Nothing to reorder right now",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "All inventory items are well-stocked above reorder thresholds.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedInk,
                                textAlign = TextAlign.Center
                            )
                            BoldButton(
                                text = "Back",
                                onClick = onBackClick,
                                isPrimary = true
                            )
                        }
                    }
                }
            }

            else -> {
                val rec = uiState.recommendation!!
                val dateSubtitle = "AI INSIGHT · " + SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(Date()).uppercase(Locale.ENGLISH)
                val windowInfo = rememberWindowSizeInfo()

                ResponsiveContainer(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    maxWidth = 640.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = windowInfo.horizontalPadding)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // 1. Header with Back Button and Title
                        val isNarrow = windowInfo.screenWidthDp < 360.dp
                        if (isNarrow) {
                            BoldTopBar(
                                title = "Reorder recommendation",
                                subtitle = dateSubtitle,
                                onBackClick = onBackClick
                            )
                            BoldButton(
                                text = "View orders",
                                onClick = onViewOrdersClick,
                                isPrimary = false,
                                borderWidth = 2.dp,
                                shadowOffset = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            BoldTopBar(
                                title = "Reorder recommendation",
                                subtitle = dateSubtitle,
                                onBackClick = onBackClick,
                                actions = {
                                    BoldButton(
                                        text = "View orders",
                                        onClick = onViewOrdersClick,
                                        isPrimary = false,
                                        borderWidth = 2.dp,
                                        shadowOffset = 2.dp,
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .heightIn(min = 48.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                                    )
                                }
                            )
                        }

                        // Short fallback notice when previously selected item is no longer in list
                        if (!uiState.selectionNotice.isNullOrBlank()) {
                            BoldCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = PaleBlue,
                                borderColor = InkDark,
                                borderWidth = 2.dp,
                                shadowOffset = 2.dp,
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Text(
                                    text = uiState.selectionNotice ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = OnPastelInk,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // 2. High Priority Badge & Why Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BoldStatusChip(status = rec.priorityText)

                            BoldButton(
                                text = "Why?",
                                onClick = { showWhyDialog = true },
                                isPrimary = false,
                                borderWidth = 2.dp,
                                shadowOffset = 2.dp,
                                modifier = Modifier.wrapContentWidth()
                            )
                        }

                        // 3. Product Info Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductAvatar(
                                name = rec.title,
                                category = "General",
                                size = 48.dp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rec.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    lineHeight = 24.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = rec.productName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedInk,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 4. Recommended Action Card
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = SoftLavender,
                            borderColor = InkDark,
                            borderWidth = 2.5.dp,
                            shadowOffset = 4.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "RECOMMENDED ACTION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (ThemeState.isDark) OnPastelInk else MainBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Reorder ${rec.recommendedUnits} units",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = OnPastelInk,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 26.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = rec.estimatedPurchaseText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnPastelMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // 5. Why This Recommendation Card
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CardWhite,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Why this recommendation?",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                MetricRow("Current stock / minimum", rec.currentStockText)
                                MetricRow("Sales · last 30 days", rec.salesLast7DaysText)
                                MetricRow("Average daily demand", rec.avgDailyDemandText)
                                MetricRow("Stock cover now", rec.stockCoverNowText)
                                MetricRow("After adding ${rec.recommendedUnits} units", rec.stockCoverAfterText)
                            }
                        }

                        // 6. Demand Description Text
                        Text(
                            text = rec.demandDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkDark,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        // 7. Order Placement Info Box
                        BoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = PaleBlue,
                            borderColor = InkDark,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(CardWhite, CircleShape)
                                        .border(2.dp, InkDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(14.dp)) {
                                        val path = Path().apply {
                                            moveTo(size.width * 0.2f, size.height * 0.52f)
                                            lineTo(size.width * 0.42f, size.height * 0.74f)
                                            lineTo(size.width * 0.8f, size.height * 0.26f)
                                        }
                                        drawPath(
                                            path = path,
                                            color = InkDark,
                                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Order is placed directly",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OnPastelInk,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "A purchase order will be created and saved to your store.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnPastelMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 8. Action Buttons (Accept / Dismiss)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BoldButton(
                                onClick = {
                                    viewModel.acceptReorder(
                                        onSuccess = { onAcceptClick() },
                                        onError = { msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                enabled = !uiState.isOrdering && rec.recommendedUnits > 0,
                                isPrimary = true,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isOrdering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = InkDark,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Accept",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            BoldButton(
                                text = "Dismiss",
                                onClick = {
                                    viewModel.dismissReorder()
                                    onDismissClick()
                                },
                                isPrimary = false,
                                modifier = Modifier.weight(1f)
                            )
                        }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (showWhyDialog) {
                        AiExplainBottomSheet(
                            type = "reorder",
                            productId = rec.productId ?: 0,
                            productName = rec.productName,
                            onDismiss = { showWhyDialog = false }
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MutedInk,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = InkDark,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReorderRecommendationScreenPreview() {
    StockSenseTheme {
        ReorderRecommendationScreen()
    }
}
