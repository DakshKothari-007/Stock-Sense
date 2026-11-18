package com.simats.stocksense.feature.reorder.ui

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
import androidx.compose.ui.geometry.Size
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
fun RecommendationPairsScreen(
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

    val pair = uiState.activePairItem
    val prod1 = pair?.product1
    val prod2 = pair?.product2

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
                    title = "Better together",
                    subtitle = "AI INSIGHT · ${NumberFormatUtil.formatCurrentHeaderDate().uppercase(Locale.ENGLISH)}",
                    onBackClick = onBackClick
                )

            if (uiState.isLoading && pair == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MainBlue)
                }
            } else if (!uiState.hasEnoughPairsHistory) {
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
                            text = uiState.productPairsHistoryMessage
                                ?: "Store needs at least 30 bills in the last 90 days to identify reliable product pairs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (pair == null) {
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
                            text = "No product pairs yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Text(
                            text = "No strong product affinities found meeting the minimum co-occurrence threshold.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                val prod1 = pair.product1
                val prod2 = pair.product2

                // 2. Category Tag & Why Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoldChip(
                        text = "Frequently bought together",
                        isSelected = true,
                        backgroundColor = SoftLightBlue,
                        textColor = InkDark,
                        borderColor = InkDark,
                        borderWidth = 1.5.dp
                    )

                    BoldButton(
                        text = "Why?",
                        onClick = { showWhyDialog = true },
                        isPrimary = false,
                        borderWidth = 1.5.dp,
                        shadowOffset = 2.dp,
                        modifier = Modifier.height(34.dp)
                    )
                }

                // 3. Headline
                val headlineText = pair.headline ?: "Often bought together: ${pair.product1?.name ?: "Product 1"} + ${pair.product2?.name ?: "Product 2"}"
                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    fontSize = 20.sp
                )

                // 4. Products Pair Card
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Product 1
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductAvatar(
                                name = prod1?.name,
                                category = prod1?.category,
                                size = 40.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${prod1?.name ?: " — "} · ${prod1?.category ?: prod1?.sku ?: " — "}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${NumberFormatUtil.formatIndianRupees(prod1?.price)} · ${prod1?.effectiveStock ?: 0} units available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        HorizontalDivider(color = InkDark.copy(alpha = 0.2f), thickness = 1.dp)

                        // Product 2
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductAvatar(
                                name = prod2?.name,
                                category = prod2?.category,
                                size = 40.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${prod2?.name ?: " — "} · ${prod2?.category ?: prod2?.sku ?: " — "}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${NumberFormatUtil.formatIndianRupees(prod2?.price)} · ${prod2?.effectiveStock ?: 0} units available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 5. Evidence Cards Row
                val supportPct = ((pair.effectiveSupport) * 100).toInt()
                val billsTogether = pair.metrics?.billsTogether ?: 0
                val totalBills = pair.metrics?.totalStoreBills ?: uiState.productPairsData?.totalBillsAnalyzed ?: 0
                val windowDays = uiState.productPairsData?.config?.windowDays ?: 90

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BoldCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = CardWhite,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Baskets with both",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$supportPct%",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$billsTogether of $totalBills baskets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    BoldCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = CardWhite,
                        borderColor = InkDark,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Evidence window",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$windowDays days",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lift: ${pair.effectiveLift}x",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 6. Recommended Action Card
                val suggAction = pair.suggestedAction
                val combinedPrice = suggAction?.combinedRegularPrice
                    ?: ((prod1?.price ?: 0.0) + (prod2?.price ?: 0.0))
                val bundlePrice = suggAction?.bundlePrice
                    ?: (combinedPrice * 0.9)
                val savings = if (combinedPrice > bundlePrice) combinedPrice - bundlePrice else 0.0

                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (ThemeState.isDark) SoftLightBlue else SoftPeach,
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
                            text = "RECOMMENDED ACTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Offer the pair at ${NumberFormatUtil.formatIndianRupees(bundlePrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 20.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Usual combined price",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                            Text(
                                text = NumberFormatUtil.formatIndianRupees(combinedPrice),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 13.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Customer saves",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 12.sp
                            )
                            Text(
                                text = NumberFormatUtil.formatIndianRupees(savings),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 13.sp
                            )
                        }

                        val placementText = suggAction?.placementTip
                            ?: "Place ${prod1?.name ?: "Product 1"} and ${prod2?.name ?: "Product 2"} together with a shelf sign. Review results after 7 days."
                        Text(
                            text = placementText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // 7. Disclaimer Note Card
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
                                color = InkDark,
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
                                color = InkDark,
                                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Decision record only",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Accepting does not publish an offer or change prices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 8. Action Buttons Row (Accept & Dismiss)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        BoldButton(
                            onClick = {
                                val p1 = prod1?.effectiveId
                                val p2 = prod2?.effectiveId
                                if (p1 != null && p2 != null && p1 > 0 && p2 > 0) {
                                    viewModel.respondProductPair(
                                        productId1 = p1,
                                        productId2 = p2,
                                        action = "accepted",
                                        onSuccess = onAcceptClick,
                                        onError = { msg ->
                                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    android.widget.Toast.makeText(context, "Invalid product pair IDs", android.widget.Toast.LENGTH_SHORT).show()
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
                                val p1 = prod1?.effectiveId
                                val p2 = prod2?.effectiveId
                                if (p1 != null && p2 != null && p1 > 0 && p2 > 0) {
                                    viewModel.respondProductPair(
                                        productId1 = p1,
                                        productId2 = p2,
                                        action = "dismissed",
                                        onSuccess = onDismissClick,
                                        onError = { msg ->
                                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    android.widget.Toast.makeText(context, "Invalid product pair IDs", android.widget.Toast.LENGTH_SHORT).show()
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

            if (showWhyDialog && prod1 != null) {
                AiExplainBottomSheet(
                    type = "pair",
                    productId = prod1.effectiveId ?: 0,
                    productName = "${prod1.name ?: ""} + ${prod2?.name ?: ""}",
                    onDismiss = { showWhyDialog = false }
                )
            }
        }
    }
}
}

@Preview(showBackground = true)
@Composable
fun RecommendationPairsScreenPreview() {
    StockSenseTheme {
        RecommendationPairsScreen()
    }
}
