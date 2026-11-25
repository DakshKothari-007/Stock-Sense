package com.simats.stocksense.feature.reorder

import com.simats.stocksense.data.model.ReorderItemDto
import com.simats.stocksense.data.model.ReorderProductDto
import com.simats.stocksense.feature.reorder.viewmodel.ReorderUiState
import com.simats.stocksense.feature.reorder.viewmodel.ReorderViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReorderSelectionTest {

    private fun makeItem(
        id: Int?,
        name: String = "Item ${id ?: 0}",
        sku: String? = id?.let { "GEN-${it.toString().padStart(3, '0')}" },
        stock: Int = 3,
        minThreshold: Int = 10,
        suggestedQty: Int = 15,
        priority: String = "high"
    ): ReorderItemDto {
        return ReorderItemDto(
            product = ReorderProductDto(
                id = id,
                name = name,
                sku = sku,
                category = "General",
                price = 50.0,
                costPrice = 30.0
            ),
            currentStock = stock,
            minThreshold = minThreshold,
            soldLast30Days = 30,
            dailySalesRate = 1.0,
            daysOfStockLeft = 3.0,
            suggestedQuantity = suggestedQty,
            estimatedCost = suggestedQty * 30.0,
            priority = priority
        )
    }

    @Test
    fun selectById_matchesExactProductRegardlessOfPosition() {
        val items = (1..5).map { makeItem(id = it * 10, name = "Shop Item $it") }
        val initial = ReorderUiState(isLoading = false, items = items)

        val (selectedState, shouldFetch) = ReorderViewModel.applySelectProduct(
            current = initial,
            productId = 40,
            sku = null
        )

        assertFalse(shouldFetch)
        assertEquals(40, selectedState.recommendation?.productId)
        assertEquals("GEN-040", selectedState.recommendation?.sku)
        assertTrue(selectedState.recommendation?.productName?.contains("Shop Item 4") == true)
        assertNull(selectedState.selectionNotice)
    }

    @Test
    fun selectBySku_matchesWhenIdIsMissing() {
        val items = listOf(
            makeItem(id = 11, name = "Alpha Pack", sku = "CAT-011"),
            makeItem(id = null, name = "Beta Pack", sku = "CAT-022"),
            makeItem(id = 33, name = "Gamma Pack", sku = "CAT-033")
        )
        val initial = ReorderUiState(isLoading = false, items = items)

        val (selectedState, shouldFetch) = ReorderViewModel.applySelectProduct(
            current = initial,
            productId = null,
            sku = " cat-022 "
        )

        assertFalse(shouldFetch)
        assertNotNull(selectedState.recommendation)
        assertEquals("CAT-022", selectedState.recommendation?.sku)
        assertTrue(selectedState.recommendation?.productName?.contains("Beta Pack") == true)
        assertNull(selectedState.selectionNotice)
    }

    @Test
    fun idAndSkuMissing_selectsFirstAvailableWithoutNotice() {
        val items = listOf(
            makeItem(id = 101, name = "First Item"),
            makeItem(id = 102, name = "Second Item")
        )
        val loaded = ReorderViewModel.applyLoadedItems(
            current = ReorderUiState(isLoading = true),
            rawItems = items
        )

        val (selectedState, shouldFetch) = ReorderViewModel.applySelectProduct(
            current = loaded,
            productId = null,
            sku = "   "
        )

        assertFalse(shouldFetch)
        assertEquals(101, selectedState.recommendation?.productId)
        assertNull(selectedState.selectionNotice)
    }

    @Test
    fun itemNotFound_showsShortNoticeAndFirstAvailableItem() {
        val freshItems = listOf(
            makeItem(id = 201, name = "Available First"),
            makeItem(id = 202, name = "Available Second")
        )
        val awaiting = ReorderUiState(
            isLoading = true,
            isAwaitingSelection = true,
            selectedTargetProductId = 999,
            selectedTargetSku = "MISSING-999"
        )

        val resultState = ReorderViewModel.applyLoadedItems(awaiting, freshItems)

        assertFalse(resultState.isLoading)
        assertFalse(resultState.isAwaitingSelection)
        assertEquals(201, resultState.recommendation?.productId)
        assertEquals(ReorderViewModel.ITEM_NOT_FOUND_NOTICE, resultState.selectionNotice)
    }

    @Test
    fun zeroItems_showsEmptyStateCleanly() {
        val awaiting = ReorderUiState(
            isLoading = true,
            isAwaitingSelection = true,
            selectedTargetProductId = 50,
            selectedTargetSku = "GEN-050"
        )

        val resultState = ReorderViewModel.applyLoadedItems(awaiting, emptyList())

        assertFalse(resultState.isLoading)
        assertFalse(resultState.isAwaitingSelection)
        assertTrue(resultState.items.isEmpty())
        assertNull(resultState.recommendation)
        assertNull(resultState.selectionNotice)
    }

    @Test
    fun singleItem_handlesMatchAndNotFoundAndDismiss() {
        val singleItem = listOf(makeItem(id = 77, name = "Only Product", sku = "ONLY-077"))

        val matched = ReorderViewModel.applyLoadedItems(
            current = ReorderUiState(isLoading = true, selectedTargetProductId = 77),
            rawItems = singleItem
        )
        assertEquals(77, matched.recommendation?.productId)
        assertNull(matched.selectionNotice)

        val fallback = ReorderViewModel.applyLoadedItems(
            current = ReorderUiState(isLoading = true, selectedTargetProductId = 888),
            rawItems = singleItem
        )
        assertEquals(77, fallback.recommendation?.productId)
        assertEquals(ReorderViewModel.ITEM_NOT_FOUND_NOTICE, fallback.selectionNotice)

        val dismissed = ReorderViewModel.applyDismissCurrent(matched)
        assertTrue(dismissed.isDismissed)
    }

    @Test
    fun tenItemsInDifferentOrder_alwaysSelectsSameProductById() {
        val originalOrder = (1..10).map { makeItem(id = 500 + it, name = "Product #$it", sku = "SKU-$it") }
        val targetId = 507

        val stateAfterSelect = ReorderViewModel.applyLoadedItems(
            current = ReorderUiState(isLoading = true, selectedTargetProductId = targetId),
            rawItems = originalOrder
        )
        assertEquals(targetId, stateAfterSelect.recommendation?.productId)
        assertEquals("SKU-7", stateAfterSelect.recommendation?.sku)

        // Simulate list order changing completely after refresh / sorting / partial accept
        val reorderedList = originalOrder.reversed().filterNot { it.product?.id == 501 || it.product?.id == 503 }
        val stateAfterReorderRefresh = ReorderViewModel.applyLoadedItems(
            current = stateAfterSelect,
            rawItems = reorderedList
        )
        assertEquals(targetId, stateAfterReorderRefresh.recommendation?.productId)
        assertEquals("SKU-7", stateAfterReorderRefresh.recommendation?.sku)
        assertNull(stateAfterReorderRefresh.selectionNotice)
    }

    @Test
    fun duplicateProductNamesWithDifferentIds_selectsByExactId() {
        val items = listOf(
            makeItem(id = 301, name = "Store Brand Rice", sku = "RICE-1KG"),
            makeItem(id = 302, name = "Store Brand Rice", sku = "RICE-5KG")
        )
        val state = ReorderViewModel.applyLoadedItems(
            current = ReorderUiState(isLoading = true, selectedTargetProductId = 302),
            rawItems = items
        )

        assertEquals(302, state.recommendation?.productId)
        assertEquals("RICE-5KG", state.recommendation?.sku)
        assertNull(state.selectionNotice)
    }

    @Test
    fun selectWhileLoading_waitsThenResolvesMatch() {
        val loadingState = ReorderUiState(isLoading = true, items = emptyList())
        val (waitingState, shouldFetch) = ReorderViewModel.applySelectProduct(
            current = loadingState,
            productId = 605,
            sku = "SKU-605"
        )

        assertFalse(shouldFetch)
        assertTrue(waitingState.isAwaitingSelection)
        assertNull(waitingState.selectionNotice)

        val arrived = (601..610).map { makeItem(id = it, name = "Stock $it", sku = "SKU-$it") }
        val resolved = ReorderViewModel.applyLoadedItems(waitingState, arrived)

        assertFalse(resolved.isLoading)
        assertFalse(resolved.isAwaitingSelection)
        assertEquals(605, resolved.recommendation?.productId)
        assertEquals("SKU-605", resolved.recommendation?.sku)
        assertNull(resolved.selectionNotice)
    }

    @Test
    fun clearSelection_resetsTargetAndNoticeAndRestoresFirstItem() {
        val items = (1..4).map { makeItem(id = it, name = "Item $it") }
        val selectedState = ReorderViewModel.applyLoadedItems(
            current = ReorderUiState(isLoading = true, selectedTargetProductId = 999),
            rawItems = items
        )
        assertEquals(ReorderViewModel.ITEM_NOT_FOUND_NOTICE, selectedState.selectionNotice)

        val cleared = ReorderViewModel.applyClearSelection(selectedState)
        assertNull(cleared.selectedTargetProductId)
        assertNull(cleared.selectedTargetSku)
        assertNull(cleared.selectionNotice)
        assertFalse(cleared.isAwaitingSelection)
        assertFalse(cleared.isDismissed)
        assertEquals(1, cleared.recommendation?.productId)
    }
}
