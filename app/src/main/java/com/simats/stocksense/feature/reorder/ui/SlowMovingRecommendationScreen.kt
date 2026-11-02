package com.simats.stocksense.feature.reorder.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.simats.stocksense.core.theme.Amber50
import com.simats.stocksense.core.theme.Amber700
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.feature.reorder.viewmodel.InsightsViewModel
import java.util.Locale

@Composable
fun SlowMovingRecommendationScreen(
    onBackClick: () -> Unit = {},
    onAcceptClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
    viewModel: InsightsViewModel = viewModel()
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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

    val item = uiState.activeSlowMovingItem

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                Column {
                    Text(
                        text = "Move slow stock",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "AI INSIGHT · ${NumberFormatUtil.formatCurrentHeaderDate().uppercase(Locale.ENGLISH)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (uiState.isLoading && item == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (!uiState.hasEnoughSlowMovingHistory) {
                // Not enough sales history state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Sales history needed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.slowMovingHistoryMessage
                                ?: "Need at least 30 days of sales history to calculate slow-moving recommendations accurately.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (item == null) {
                // Empty state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Nothing slow-moving right now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "All products in your store are selling within healthy inventory cover thresholds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // 2. Category Tag
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Amber50
                ) {
                    Text(
                        text = "Slow-moving inventory",
                        style = MaterialTheme.typography.labelSmall,
                        color = Amber700,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 3. Product Title Header Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(22.dp)) {
                            drawCircle(
                                color = primaryColor,
                                radius = size.width * 0.45f,
                                center = Offset(size.width / 2f, size.height / 2f),
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                            drawCircle(color = primaryColor, radius = size.width * 0.08f, center = Offset(size.width * 0.35f, size.height * 0.35f))
                            drawCircle(color = primaryColor, radius = size.width * 0.08f, center = Offset(size.width * 0.65f, size.height * 0.45f))
                            drawCircle(color = primaryColor, radius = size.width * 0.08f, center = Offset(size.width * 0.45f, size.height * 0.65f))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Make room for demand",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.name ?: " — "} · ${item.category ?: item.sku ?: " — "}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // 4. What your sales tell us Card
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "What your sales tell us",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        val soldPacks = item.unitsSold30Days ?: 0
                        val revenue = if (item.sellingPrice != null) soldPacks * item.sellingPrice!! else null
                        val coverDaysText = when {
                            item.stockCoverDays != null -> "${item.stockCoverDays!!.toInt()} days"
                            soldPacks == 0 -> "No sales in 30 days"
                            else -> " — "
                        }

                        StatRow("Current stock", "${item.effectiveStock} packs")
                        StatRow("Sold in 30 days", "$soldPacks packs · ${if (revenue != null) NumberFormatUtil.formatIndianRupees(revenue) else " — "}")
                        StatRow("Cash tied up · At cost", NumberFormatUtil.formatIndianRupees(item.tiedUpCapital))
                        StatRow("Stock cover at current pace", coverDaysText)
                    }
                }

                // 5. Recommended Offer Card
                val discPct = item.effectiveDiscountPct
                val suggPrice = item.effectiveSuggestedPrice
                val sellPrice = item.sellingPrice
                val costPrice = item.costPrice
                val grossMargin = if (suggPrice != null && costPrice != null) suggPrice - costPrice else null

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "RECOMMENDED · 7-DAY TRIAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "$discPct% off · ${NumberFormatUtil.formatIndianRupees(suggPrice)} per pack",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp
                        )

                        val explanationText = when {
                            costPrice != null && grossMargin != null && grossMargin >= 0 ->
                                "Down from ${NumberFormatUtil.formatIndianRupees(sellPrice)}. Keep ${NumberFormatUtil.formatIndianRupees(grossMargin)} gross margin over the ${NumberFormatUtil.formatIndianRupees(costPrice)} purchase cost."
                            costPrice != null && grossMargin != null && grossMargin < 0 ->
                                "Down from ${NumberFormatUtil.formatIndianRupees(sellPrice)}. Warning: ${NumberFormatUtil.formatIndianRupees(-grossMargin)} below the ${NumberFormatUtil.formatIndianRupees(costPrice)} purchase cost."
                            else ->
                                "Down from ${NumberFormatUtil.formatIndianRupees(sellPrice)}. Purchase cost unknown."
                        }

                        Text(
                            text = explanationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        val bundleSugg = item.clearanceSuggestion?.bundleSuggestion
                        val bundlePrice = bundleSugg?.suggestedPrice ?: (if (sellPrice != null) sellPrice * 0.85 else null)
                        val bundlePct = bundleSugg?.discountPct ?: 15

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = primaryColor.copy(alpha = 0.2f), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alternative clearance bundle",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Text(
                                text = NumberFormatUtil.formatIndianRupees(bundlePrice),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "$bundlePct% bundle discount: ${NumberFormatUtil.formatIndianRupees(bundlePrice)} per unit. Display together near the counter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // 6. Suggestion Disclaimer Note Card
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
                                text = "A suggestion, not a sales guarantee",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Acceptance records your decision. No prices change automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 7. Action Buttons Row (Accept & Dismiss)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val pid = item.productId
                            if (pid != null && pid > 0) {
                                viewModel.respondSlowMoving(
                                    productId = pid,
                                    action = "accepted",
                                    onSuccess = onAcceptClick,
                                    onError = { msg ->
                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                android.widget.Toast.makeText(context, "Invalid product ID", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        enabled = !uiState.isResponding,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = if (uiState.isResponding) "Saving..." else "Accept",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val pid = item.productId
                            if (pid != null && pid > 0) {
                                viewModel.respondSlowMoving(
                                    productId = pid,
                                    action = "dismissed",
                                    onSuccess = onDismissClick,
                                    onError = { msg ->
                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                android.widget.Toast.makeText(context, "Invalid product ID", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        enabled = !uiState.isResponding,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SlowMovingRecommendationScreenPreview() {
    StockSenseTheme {
        SlowMovingRecommendationScreen()
    }
}
