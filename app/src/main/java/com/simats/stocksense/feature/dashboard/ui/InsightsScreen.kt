package com.simats.stocksense.feature.dashboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.simats.stocksense.data.model.ProductPairItemDto
import com.simats.stocksense.data.model.ReorderItemDto
import com.simats.stocksense.data.model.SlowMovingItemDto
import com.simats.stocksense.feature.inventory.icon.ProductAvatar
import com.simats.stocksense.feature.reorder.viewmodel.InsightsUiState
import com.simats.stocksense.feature.reorder.viewmodel.InsightsViewModel
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale

/**
 * Filter categories for AI recommendations.
 */
enum class InsightFilterCategory(val label: String) {
    ALL("All"),
    REORDER("Reorder"),
    SLOW_MOVING("Slow-moving"),
    BUNDLE("Bundle")
}

/**
 * Unified model for rendering diverse AI insights sorted by priority.
 */
sealed class InsightItem {
    abstract val id: String
    abstract val priorityRank: Int // 1 = High, 2 = Medium, 3 = Low
    abstract val category: InsightFilterCategory

    data class Reorder(
        val item: ReorderItemDto,
        val originalIndex: Int
    ) : InsightItem() {
        override val id: String = "reorder_${item.product?.id ?: originalIndex}_${item.product?.sku ?: ""}"
        override val category: InsightFilterCategory = InsightFilterCategory.REORDER
        override val priorityRank: Int
            get() = when (item.priority?.lowercase(Locale.ENGLISH)) {
                "high" -> 1
                "medium" -> 2
                "low" -> 3
                else -> if ((item.currentStock ?: 0) <= (item.minThreshold ?: 0)) 1 else 2
            }
    }

    data class SlowMoving(
        val item: SlowMovingItemDto,
        val originalIndex: Int
    ) : InsightItem() {
        override val id: String = "slow_${item.productId ?: originalIndex}_${item.sku ?: ""}"
        override val category: InsightFilterCategory = InsightFilterCategory.SLOW_MOVING
        override val priorityRank: Int
            get() = if ((item.unitsSold30Days ?: 0) == 0 || item.ruleTriggered?.contains("dead", ignoreCase = true) == true) {
                1 // Dead stock has highest urgency
            } else {
                2 // Standard slow moving
            }
    }

    data class ProductPair(
        val item: ProductPairItemDto,
        val originalIndex: Int
    ) : InsightItem() {
        override val id: String = "pair_${item.pairId ?: "${item.product1?.id}_${item.product2?.id}_$originalIndex"}"
        override val category: InsightFilterCategory = InsightFilterCategory.BUNDLE
        override val priorityRank: Int = 3 // Bundle cross-sell opportunity
    }
}

@Composable
fun InsightsContent(
    storeName: String? = null,
    onReorderMilkClick: () -> Unit = {},
    onMoveBiscuitsClick: () -> Unit = {},
    onTeaPairClick: () -> Unit = {},
    viewModel: InsightsViewModel = viewModel()
) {
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

    InsightsListContent(
        storeName = storeName,
        uiState = uiState,
        onRefresh = { viewModel.refresh() },
        onReorderMilkClick = onReorderMilkClick,
        onMoveBiscuitsClick = onMoveBiscuitsClick,
        onTeaPairClick = onTeaPairClick,
        onSlowMovingIndexSelected = { viewModel.selectSlowMovingIndex(it) },
        onPairIndexSelected = { viewModel.selectPairIndex(it) }
    )
}

@Composable
fun InsightsListContent(
    storeName: String? = null,
    uiState: InsightsUiState,
    onRefresh: () -> Unit = {},
    onReorderMilkClick: () -> Unit = {},
    onMoveBiscuitsClick: () -> Unit = {},
    onTeaPairClick: () -> Unit = {},
    onSlowMovingIndexSelected: (Int) -> Unit = {},
    onPairIndexSelected: (Int) -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Selected filter chip state
    var selectedFilter by rememberSaveable { mutableStateOf(InsightFilterCategory.ALL) }

    // Aggregate and sort all actionable insights: Reorder, Slow-moving / Dead stock, and top 5 Bundle suggestions
    val allInsights = remember(uiState.reorderItems, uiState.slowMovingItems, uiState.productPairItems) {
        val list = mutableListOf<InsightItem>()
        uiState.reorderItems.forEachIndexed { index, item ->
            list.add(InsightItem.Reorder(item, index))
        }
        uiState.slowMovingItems.forEachIndexed { index, item ->
            list.add(InsightItem.SlowMoving(item, index))
        }
        // Take at most top 5 bundle/pair suggestions (never count every product pair)
        uiState.productPairItems.take(5).forEachIndexed { index, item ->
            list.add(InsightItem.ProductPair(item, index))
        }
        list.sortedWith(compareBy<InsightItem> { it.priorityRank }.thenBy { it.id })
    }

    // Filter insights dynamically according to the selected chip
    val filteredInsights = remember(allInsights, selectedFilter) {
        when (selectedFilter) {
            InsightFilterCategory.ALL -> allInsights
            InsightFilterCategory.REORDER -> allInsights.filter { it is InsightItem.Reorder }
            InsightFilterCategory.SLOW_MOVING -> allInsights.filter { it is InsightItem.SlowMoving }
            InsightFilterCategory.BUNDLE -> allInsights.filter { it is InsightItem.ProductPair }
        }
    }

    // Tab counts matching their lists
    val allCount = allInsights.size
    val reorderCount = remember(allInsights) { allInsights.count { it is InsightItem.Reorder } }
    val slowMovingCount = remember(allInsights) { allInsights.count { it is InsightItem.SlowMoving } }
    val bundleCount = remember(allInsights) { allInsights.count { it is InsightItem.ProductPair } }

    // Header count equals the number of actionable cards in "All" tab
    val oppText = when (allCount) {
        1 -> "1 opportunity for your shop"
        else -> "$allCount opportunities for your shop"
    }
    val windowInfo = rememberWindowSizeInfo()

    ResponsiveContainer(
        modifier = Modifier.fillMaxSize(),
        maxWidth = 720.dp
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = windowInfo.horizontalPadding),
            contentPadding = PaddingValues(top = 8.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top section: Header, Opportunities Banner, and Filter Chips
            item(key = "header_section") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Header
                    Column {
                        Text(
                            text = "AI recommendations",
                            style = MaterialTheme.typography.headlineSmall,
                            color = InkDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                        val storeDisplay = storeName?.takeIf { it.isNotBlank() } ?: "Store"
                        Text(
                            text = "$storeDisplay · ${NumberFormatUtil.formatCurrentHeaderDate()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Summary Opportunities Banner
                    BoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (ThemeState.isDark) SoftLavender else SoftPeach,
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
                                    .size(36.dp)
                                    .background(CardWhite, CircleShape)
                                    .border(2.dp, InkDark, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(16.dp)) {
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

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = oppText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Based on stock levels and recent sales.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InsightFilterCategory.values().forEach { filter ->
                            val isSelected = selectedFilter == filter
                            val count = when (filter) {
                                InsightFilterCategory.ALL -> allCount
                                InsightFilterCategory.REORDER -> reorderCount
                                InsightFilterCategory.SLOW_MOVING -> slowMovingCount
                                InsightFilterCategory.BUNDLE -> bundleCount
                            }
                            BoldChip(
                                text = "${filter.label} ($count)",
                                isSelected = isSelected,
                                onClick = { selectedFilter = filter }
                            )
                        }
                    }
                }
            }

        // Content Area: Loading / Error / Empty State / List of Cards
        if (uiState.isLoading && allInsights.isEmpty()) {
            item(key = "loading_state") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MainBlue)
                }
            }
        } else if (uiState.errorMessage != null && allInsights.isEmpty()) {
            item(key = "error_state") {
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Failed to load insights.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusDangerRed,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        BoldButton(
                            text = "Retry",
                            onClick = onRefresh,
                            isPrimary = true
                        )
                    }
                }
            }
        } else if (filteredInsights.isEmpty()) {
            item(key = "empty_state") {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No opportunities right now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        val emptyMessage = when (selectedFilter) {
                            InsightFilterCategory.ALL -> "Everything is well stocked and selling normally. Check back as new sales occur."
                            InsightFilterCategory.REORDER -> "All products have adequate stock levels. No reorders needed right now."
                            InsightFilterCategory.SLOW_MOVING -> if (!uiState.hasEnoughSlowMovingHistory) {
                                uiState.slowMovingHistoryMessage
                                    ?: "Need at least 30 days of sales history to calculate slow-moving recommendations accurately."
                            } else {
                                "Nothing slow-moving right now. All inventory is turning over on schedule."
                            }
                            InsightFilterCategory.BUNDLE -> if (!uiState.hasEnoughPairsHistory) {
                                uiState.productPairsHistoryMessage
                                    ?: "Store needs at least 30 bills in the last 90 days to identify reliable product pairs."
                            } else {
                                "No bundle combinations detected right now."
                            }
                        }
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            // Virtualized items list - supports large lists (50+) smoothly
            items(
                items = filteredInsights,
                key = { it.id }
            ) { insight ->
                when (insight) {
                    is InsightItem.Reorder -> {
                        ReorderInsightCard(
                            reorder = insight.item,
                            primaryColor = primaryColor,
                            onClick = onReorderMilkClick
                        )
                    }
                    is InsightItem.SlowMoving -> {
                        SlowMovingInsightCard(
                            item = insight.item,
                            primaryColor = primaryColor,
                            onClick = {
                                onSlowMovingIndexSelected(insight.originalIndex)
                                onMoveBiscuitsClick()
                            }
                        )
                    }
                    is InsightItem.ProductPair -> {
                        ProductPairInsightCard(
                            pair = insight.item,
                            primaryColor = primaryColor,
                            onClick = {
                                onPairIndexSelected(insight.originalIndex)
                                onTeaPairClick()
                            }
                        )
                    }
                }
            }
        }

        // Footer Note: Placed at the end of the scrollable list
        item(key = "footer_note") {
            Text(
                text = "AI suggests. You decide. Review the evidence before taking action.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )
        }

        // Bottom spacer to ensure Dukaan Mitra FAB never covers content
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
}

/**
 * Reorder Recommendation Card
 */
@Composable
fun ReorderInsightCard(
    reorder: ReorderItemDto,
    primaryColor: Color,
    onClick: () -> Unit
) {
    BoldCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CardWhite,
        borderColor = InkDark,
        borderWidth = 2.dp,
        shadowOffset = 3.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    ProductAvatar(
                        name = reorder.product?.name,
                        category = reorder.product?.category,
                        size = 42.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val productName = reorder.product?.name ?: "Stock"
                        Text(
                            text = "Reorder $productName",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val priorityTag = when {
                            (reorder.currentStock ?: 0) <= 0 -> "OUT OF STOCK · REORDER"
                            reorder.priority?.equals("high", ignoreCase = true) == true -> "HIGH PRIORITY · REORDER"
                            reorder.priority?.equals("medium", ignoreCase = true) == true -> "MEDIUM PRIORITY · REORDER"
                            else -> "${(reorder.priority ?: "LOW").uppercase(Locale.ENGLISH)} PRIORITY · REORDER"
                        }
                        Text(
                            text = priorityTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MainBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "→",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = InkDark
                )
            }

            val currentStock = reorder.currentStock ?: 0
            val salesRate = reorder.dailySalesRate ?: 0.0
            val suggestedQty = reorder.suggestedQuantity ?: 0
            val costStr = NumberFormatUtil.formatIndianRupees(reorder.estimatedCost)
            val salesRateFormatted = if (salesRate % 1.0 == 0.0) "${salesRate.toInt()}" else String.format(Locale.US, "%.1f", salesRate)
            val stockText = "$currentStock units left, $salesRateFormatted sold per day. Add $suggestedQty units for $costStr to avoid an empty shelf."
            Text(
                text = stockText,
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

/**
 * Slow-Moving / Dead Stock Recommendation Card
 */
@Composable
fun SlowMovingInsightCard(
    item: SlowMovingItemDto,
    primaryColor: Color,
    onClick: () -> Unit
) {
    BoldCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CardWhite,
        borderColor = InkDark,
        borderWidth = 2.dp,
        shadowOffset = 3.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    ProductAvatar(
                        name = item.name,
                        category = null,
                        size = 42.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val itemName = item.name ?: "stock"
                        Text(
                            text = "Move $itemName faster",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val isDeadStock = (item.unitsSold30Days ?: 0) == 0 || item.ruleTriggered?.contains("dead", ignoreCase = true) == true
                        val slowTag = if (isDeadStock) "HIGH PRIORITY · DEAD STOCK" else "MEDIUM PRIORITY · SLOW-MOVING"
                        Text(
                            text = slowTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusLowStock,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "→",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = InkDark
                )
            }

            val slowText = "${item.effectiveStock} packs left; only ${item.unitsSold30Days ?: 0} sold in 30 days. Try ${item.effectiveDiscountPct}% off or a clearance offer."
            Text(
                text = slowText,
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

/**
 * Frequently Bought Together Recommendation Card
 */
@Composable
fun ProductPairInsightCard(
    pair: ProductPairItemDto,
    primaryColor: Color,
    onClick: () -> Unit
) {
    BoldCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CardWhite,
        borderColor = InkDark,
        borderWidth = 2.dp,
        shadowOffset = 3.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    ProductAvatar(
                        name = pair.product1?.name ?: pair.product2?.name,
                        category = null,
                        size = 42.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val p1Name = pair.product1?.name ?: "Product 1"
                        val p2Name = pair.product2?.name ?: "Product 2"
                        val pairTitle = "$p1Name + $p2Name"
                        Text(
                            text = pairTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "FREQUENTLY BOUGHT TOGETHER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MainBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "→",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = InkDark
                )
            }

            val p1Name = pair.product1?.name ?: "Product 1"
            val p2Name = pair.product2?.name ?: "Product 2"
            val supportPct = ((pair.effectiveSupport) * 100).toInt()
            val bundlePrice = pair.suggestedAction?.bundlePrice ?: 0.0
            val pairDetail = "$p1Name + $p2Name appear together in $supportPct% of baskets. Offer the pair at ${NumberFormatUtil.formatIndianRupees(bundlePrice)}."
            Text(
                text = pairDetail,
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InsightsScreenPreview() {
    StockSenseTheme(themeMode = ThemeMode.LIGHT) {
        InsightsListContent(
            storeName = "Kirana Store",
            uiState = InsightsUiState()
        )
    }
}

@Preview(name = "Insights Dark Mode", showBackground = true)
@Composable
fun InsightsScreenDarkPreview() {
    StockSenseTheme(themeMode = ThemeMode.DARK) {
        InsightsListContent(
            storeName = "Kirana Store",
            uiState = InsightsUiState()
        )
    }
}

@Preview(name = "Insights Small 360dp Large Font", showBackground = true, widthDp = 360, fontScale = 1.3f)
@Composable
fun InsightsScreenSmallFontPreview() {
    StockSenseTheme(themeMode = ThemeMode.LIGHT) {
        InsightsListContent(
            storeName = "Kirana Store",
            uiState = InsightsUiState()
        )
    }
}
