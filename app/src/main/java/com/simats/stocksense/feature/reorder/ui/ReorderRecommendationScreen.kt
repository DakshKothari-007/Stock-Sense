package com.simats.stocksense.feature.reorder.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.theme.*
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            uiState.isLoading && uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Checking reorder recommendations...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Unable to load recommendations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(32.dp)) {
                                val path = Path().apply {
                                    moveTo(size.width * 0.2f, size.height * 0.52f)
                                    lineTo(size.width * 0.42f, size.height * 0.74f)
                                    lineTo(size.width * 0.8f, size.height * 0.26f)
                                }
                                drawCircle(color = primaryColor, radius = size.width / 2f, style = Stroke(width = 2.dp.toPx()))
                                drawPath(path = path, color = primaryColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                        }
                        Text(
                            text = "Nothing to reorder right now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "All inventory items are well-stocked above reorder thresholds.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onBackClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back")
                        }
                    }
                }
            }

            else -> {
                val rec = uiState.recommendation!!
                val dateSubtitle = "AI INSIGHT · " + SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(Date()).uppercase(Locale.ENGLISH)

                val (badgeBg, badgeText) = when (rec.priority) {
                    "high" -> Amber50 to Amber700
                    "medium" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
                    else -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Header with Back Button and Title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
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
                                    color = primaryColor,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reorder recommendation",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = dateSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // "View orders" button in header
                        Surface(
                            onClick = onViewOrdersClick,
                            shape = RoundedCornerShape(12.dp),
                            color = primaryColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "View orders",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // 2. High Priority Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = rec.priorityText,
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // 3. Product Info Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(24.dp)) {
                                drawRect(
                                    color = primaryColor,
                                    topLeft = Offset(size.width * 0.35f, 0f),
                                    size = Size(size.width * 0.3f, size.height * 0.25f),
                                    style = Stroke(width = 1.8.dp.toPx())
                                )
                                drawRect(
                                    color = primaryColor,
                                    topLeft = Offset(size.width * 0.2f, size.height * 0.25f),
                                    size = Size(size.width * 0.6f, size.height * 0.75f),
                                    style = Stroke(width = 1.8.dp.toPx())
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = rec.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = rec.productName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 4. Recommended Action Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "RECOMMENDED ACTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Reorder ${rec.recommendedUnits} units",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = rec.estimatedPurchaseText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // 5. Why This Recommendation Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                                color = MaterialTheme.colorScheme.onSurface,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // 7. Order Placement Info Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                drawCircle(
                                    color = primaryColor,
                                    radius = size.width / 2f,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                                val path = Path().apply {
                                    moveTo(size.width * 0.25f, size.height * 0.5f)
                                    lineTo(size.width * 0.45f, size.height * 0.7f)
                                    lineTo(size.width * 0.75f, size.height * 0.35f)
                                }
                                drawPath(
                                    path = path,
                                    color = primaryColor,
                                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Order is placed directly",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "A purchase order will be created and saved to your store.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        Button(
                            onClick = {
                                viewModel.acceptReorder(
                                    onSuccess = { onAcceptClick() },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !uiState.isOrdering && rec.recommendedUnits > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            )
                        ) {
                            if (uiState.isOrdering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Accept",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.dismissReorder()
                                onDismissClick()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = primaryColor
                            )
                        ) {
                            Text(
                                text = "Dismiss",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
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
