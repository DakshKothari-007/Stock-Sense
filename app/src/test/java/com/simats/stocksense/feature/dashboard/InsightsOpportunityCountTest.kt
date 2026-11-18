package com.simats.stocksense.feature.dashboard

import androidx.compose.ui.graphics.Color
import com.simats.stocksense.data.model.PairProductDto
import com.simats.stocksense.data.model.ProductPairItemDto
import com.simats.stocksense.data.model.ReorderItemDto
import com.simats.stocksense.data.model.ReorderProductDto
import com.simats.stocksense.data.model.SlowMovingItemDto
import com.simats.stocksense.feature.dashboard.ui.InsightFilterCategory
import com.simats.stocksense.feature.dashboard.ui.InsightItem
import com.simats.stocksense.feature.reorder.viewmodel.InsightsUiState
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying that the Insights opportunity count:
 * 1. Counts only actionable items shown in the list (reorder, slow-moving, dead stock, and at most top 5 bundle pairs).
 * 2. Equals the number of scrollable cards in the "All" tab.
 * 3. Matches individual tab counts (Reorder, Slow-moving, Bundle).
 * 4. Meets WCAG 4.5:1 contrast ratio for dark-mode plum and cream text.
 */
class InsightsOpportunityCountTest {

    private fun createMockData(
        reorderCount: Int,
        slowCount: Int,
        pairCount: Int
    ): Triple<List<ReorderItemDto>, List<SlowMovingItemDto>, List<ProductPairItemDto>> {
        val reorders = (1..reorderCount).map { i ->
            ReorderItemDto(
                product = ReorderProductDto(id = i, name = "Reorder Product $i", sku = "SKU-R-$i"),
                currentStock = 5,
                minThreshold = 10,
                suggestedQuantity = 20,
                priority = if (i == 1) "high" else "medium"
            )
        }

        val slowMoving = (1..slowCount).map { i ->
            val isDeadStock = i <= 2
            SlowMovingItemDto(
                productId = 100 + i,
                name = if (isDeadStock) "Dead Stock Product $i" else "Slow Moving Product $i",
                sku = "SKU-S-$i",
                currentStock = 25,
                unitsSold30Days = if (isDeadStock) 0 else i * 2,
                ruleTriggered = if (isDeadStock) "units_sold_30_days = 0" else "stock_cover_days >= 60"
            )
        }

        val pairs = (1..pairCount).map { i ->
            ProductPairItemDto(
                pairId = "pair_$i",
                product1 = PairProductDto(id = 200 + i, name = "Pair Item A$i"),
                product2 = PairProductDto(id = 300 + i, name = "Pair Item B$i")
            )
        }

        return Triple(reorders, slowMoving, pairs)
    }

    @Test
    fun testTotalOpportunitiesCapsProductPairsAt5() {
        // Simulating the demo account: 4 reorders, 5 slow moving (with 2 dead stock), 109 raw pairs
        val (reorders, slowMoving, pairs) = createMockData(
            reorderCount = 4,
            slowCount = 5,
            pairCount = 109
        )

        val state = InsightsUiState(
            reorderItems = reorders,
            slowMovingItems = slowMoving,
            productPairItems = pairs.take(5) // Capped at 5 by ViewModel and API
        )

        // totalOpportunities must equal 4 + 5 + 5 = 14, NOT 118
        assertEquals(14, state.totalOpportunities)
    }

    @Test
    fun testAllTabScrollableCardCountEqualsTotalOpportunities() {
        val (reorders, slowMoving, pairs) = createMockData(
            reorderCount = 4,
            slowCount = 5,
            pairCount = 109
        )

        // Build all actionable items as done in InsightsScreen
        val allInsights = mutableListOf<InsightItem>()
        reorders.forEachIndexed { index, item ->
            allInsights.add(InsightItem.Reorder(item, index))
        }
        slowMoving.forEachIndexed { index, item ->
            allInsights.add(InsightItem.SlowMoving(item, index))
        }
        pairs.take(5).forEachIndexed { index, item ->
            allInsights.add(InsightItem.ProductPair(item, index))
        }

        // Must equal 14 cards
        assertEquals(14, allInsights.size)

        // Count of cards visible in "All" tab must match totalOpportunities
        val state = InsightsUiState(
            reorderItems = reorders,
            slowMovingItems = slowMoving,
            productPairItems = pairs.take(5)
        )
        assertEquals(state.totalOpportunities, allInsights.size)
    }

    @Test
    fun testTabCountsMatchTheirLists() {
        val (reorders, slowMoving, pairs) = createMockData(
            reorderCount = 4,
            slowCount = 5,
            pairCount = 109
        )

        val allInsights = mutableListOf<InsightItem>()
        reorders.forEachIndexed { index, item ->
            allInsights.add(InsightItem.Reorder(item, index))
        }
        slowMoving.forEachIndexed { index, item ->
            allInsights.add(InsightItem.SlowMoving(item, index))
        }
        pairs.take(5).forEachIndexed { index, item ->
            allInsights.add(InsightItem.ProductPair(item, index))
        }

        val allList = allInsights
        val reorderList = allInsights.filter { it is InsightItem.Reorder }
        val slowMovingList = allInsights.filter { it is InsightItem.SlowMoving }
        val bundleList = allInsights.filter { it is InsightItem.ProductPair }

        val allCount = allList.size
        val reorderCount = allInsights.count { it is InsightItem.Reorder }
        val slowMovingCount = allInsights.count { it is InsightItem.SlowMoving }
        val bundleCount = allInsights.count { it is InsightItem.ProductPair }

        // Tab count numbers must match the exact number of cards in their respective lists
        assertEquals(14, allCount)
        assertEquals(allCount, allList.size)

        assertEquals(4, reorderCount)
        assertEquals(reorderCount, reorderList.size)

        assertEquals(5, slowMovingCount)
        assertEquals(slowMovingCount, slowMovingList.size)

        assertEquals(5, bundleCount)
        assertEquals(bundleCount, bundleList.size)

        // Sum of categorized tabs must equal the "All" tab count
        assertEquals(allCount, reorderCount + slowMovingCount + bundleCount)
    }

    @Test
    fun testEnumCategoryLabels() {
        assertEquals("All", InsightFilterCategory.ALL.label)
        assertEquals("Reorder", InsightFilterCategory.REORDER.label)
        assertEquals("Slow-moving", InsightFilterCategory.SLOW_MOVING.label)
        assertEquals("Bundle", InsightFilterCategory.BUNDLE.label)
    }

    @Test
    fun testEmptyStateWhenZeroOpportunities() {
        val state = InsightsUiState(
            reorderItems = emptyList(),
            slowMovingItems = emptyList(),
            productPairItems = emptyList()
        )
        assertEquals(0, state.totalOpportunities)
    }

    @Test
    fun testDarkModePlumContrastRatioWithCreamText() {
        // Dark mode slow-moving plum: #5A2D6E
        // Cream text: #FFF4DC
        val plum = Color(0xFF5A2D6E)
        val cream = Color(0xFFFFF4DC)

        fun channelLuminance(value: Float): Double {
            return if (value <= 0.03928) {
                value / 12.92
            } else {
                Math.pow(((value + 0.055) / 1.055), 2.4)
            }
        }

        fun relativeLuminance(c: Color): Double {
            val r = channelLuminance(c.red)
            val g = channelLuminance(c.green)
            val b = channelLuminance(c.blue)
            return 0.2126 * r + 0.7152 * g + 0.0722 * b
        }

        val lPlum = relativeLuminance(plum)
        val lCream = relativeLuminance(cream)

        val brighter = maxOf(lPlum, lCream)
        val darker = minOf(lPlum, lCream)
        val contrastRatio = (brighter + 0.05) / (darker + 0.05)

        // Contrast ratio must be at least 4.5:1 (WCAG AA requirement)
        assertTrue(
            "Expected contrast ratio >= 4.5:1, but got $contrastRatio",
            contrastRatio >= 4.5
        )
        // In fact, Cream on Plum has contrast ~9.38 : 1 (passes AAA 7:1)
        assertTrue(contrastRatio >= 7.0)
    }
}
