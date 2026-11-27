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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
import com.simats.stocksense.feature.ai.ui.AiExplainBottomSheet
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.reorder.viewmodel.InsightsViewModel
import java.util.Locale

@Composable
fun SlowMovingRecommendationScreen(
    onBackClick: () -> Unit = {},
    onAcceptClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
    viewModel: InsightsViewModel = viewModel()
) {
    val primaryColor = MainYellow
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showWhyDialog by remember { mutableStateOf(false) }

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
        color = CreamBackground
    ) {
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

                // 1. Header with Shared BoldTopBar
                BoldTopBar(
                    title = "Move slow stock",
                    subtitle = "AI INSIGHT · ${NumberFormatUtil.formatCurrentHeaderDate().uppercase(Locale.ENGLISH)}",
                    onBackClick = onBackClick
                )

            if (uiState.isLoading && item == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MainBlue)
                }
            } else if (!uiState.hasEnoughSlowMovingHistory) {
                // Not enough sales history state
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Sales history needed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Text(
                            text = uiState.slowMovingHistoryMessage
                                ?: "Need at least 30 days of sales history to calculate slow-moving recommendations accurately.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (item == null) {
                // Empty state
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Nothing slow-moving right now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Text(
                            text = "All products in your store are selling within healthy inventory cover thresholds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // 2. Category Tag
                BoldStatusChip(
                    status = "SLOW-MOVING INVENTORY"
                )

                // 3. Product Title Header Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductAvatar(
                        name = item.name,
                        category = item.category,
                        size = 48.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Make room for demand",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.name ?: " — "} · ${item.category ?: item.sku ?: " — "}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 4. What your sales tell us Card
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isNarrow = windowInfo.screenWidthDp < 360.dp
                        if (isNarrow) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "What your sales tell us",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 15.sp
                                )
                                BoldButton(
                                    text = "Why?",
                                    onClick = { showWhyDialog = true },
                                    isPrimary = false,
                                    borderWidth = 1.5.dp,
                                    shadowOffset = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "What your sales tell us",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BoldButton(
                                    text = "Why?",
                                    onClick = { showWhyDialog = true },
                                    isPrimary = false,
                                    borderWidth = 1.5.dp,
                                    shadowOffset = 2.dp,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        val soldPacks = item.unitsSold30Days ?: 0
                        val revenue = if (item.sellingPrice != null) soldPacks * item.sellingPrice!! else null
                        val coverDaysText = when {
                            soldPacks == 0 -> "No sales in 30 days"
                            item.stockCoverDays != null && item.stockCoverDays!! <= 365.0 -> {
                                val rounded = Math.round(item.stockCoverDays!!).toInt()
                                "$rounded days"
                            }
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

                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (ThemeState.isDark) SoftLavender else SoftPeach,
                    borderColor = InkDark,
                    borderWidth = 2.5.dp,
                    shadowOffset = 4.dp
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
                            color = OnPastelInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "$discPct% off · ${NumberFormatUtil.formatIndianRupees(suggPrice)} per pack",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnPastelInk,
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
                            color = OnPastelMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        val bundleSugg = item.clearanceSuggestion?.bundleSuggestion
                        val bundlePrice = bundleSugg?.suggestedPrice ?: (if (sellPrice != null) sellPrice * 0.85 else null)
                        val bundlePct = bundleSugg?.discountPct ?: 15

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = OnPastelDivider, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alternative clearance bundle",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnPastelMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                text = NumberFormatUtil.formatIndianRupees(bundlePrice),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnPastelInk,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "$bundlePct% bundle discount: ${NumberFormatUtil.formatIndianRupees(bundlePrice)} per unit. Display together near the counter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnPastelMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // 6. Suggestion Disclaimer Note Card
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
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawCircle(
                                color = OnPastelIcon,
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
                                color = OnPastelIcon,
                                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "A suggestion, not a sales guarantee",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnPastelInk,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Acceptance records your decision. No prices change automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnPastelMuted,
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
                    Box(modifier = Modifier.weight(1f)) {
                        BoldButton(
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
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isResponding,
                            isPrimary = true
                        ) {
                            Text(
                                text = if (uiState.isResponding) "Saving..." else "Accept",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        BoldButton(
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
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isResponding,
                            isPrimary = false
                        ) {
                            Text(
                                text = "Dismiss",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (showWhyDialog && item != null) {
                AiExplainBottomSheet(
                    type = "slow_moving",
                    productId = item.productId ?: 0,
                    productName = item.name ?: "",
                    onDismiss = { showWhyDialog = false }
                )
            }
        }
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
            color = MutedInk,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            fontSize = 12.sp,
            maxLines = 1
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
