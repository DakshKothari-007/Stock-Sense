package com.simats.stocksense.feature.reorder

import com.google.gson.Gson
import com.simats.stocksense.data.model.PurchaseOrderDto
import com.simats.stocksense.data.model.PurchaseOrderItemDto
import com.simats.stocksense.data.model.PurchaseOrdersListResponse
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying that Purchase Order cards display the first product name,
 * "+N more", and the correct product icon key.
 */
class PurchaseOrderCardDisplayTest {

    private val gson = Gson()

    @Test
    fun testSingleItemOrder_showsProductNameAndNoMoreLabel() {
        val item = PurchaseOrderItemDto(
            itemId = 1,
            productId = 3190,
            name = "Nandini Pure Ghee 500ml",
            category = "Dairy",
            iconKey = "milk_bottle",
            quantity = 19
        )
        val order = PurchaseOrderDto(
            orderNo = "PO-2026-003",
            totalItems = 1,
            totalUnits = 19,
            items = listOf(item)
        )

        assertEquals("Nandini Pure Ghee 500ml", order.cardProductName)
        assertEquals("Dairy", order.cardCategory)
        assertEquals("milk_bottle", order.cardIconKey)

        val totalItemsCount = order.totalItems ?: order.items?.size ?: 0
        val extraItemsCount = if (totalItemsCount > 1) totalItemsCount - 1 else maxOf(0, (order.items?.size ?: 1) - 1)
        assertEquals(0, extraItemsCount)

        val productTitle = if (extraItemsCount > 0 && order.cardProductName != "—") {
            "${order.cardProductName} + $extraItemsCount more"
        } else {
            order.cardProductName
        }
        assertEquals("Nandini Pure Ghee 500ml", productTitle)
    }

    @Test
    fun testMultiItemOrder_showsFirstProductNamePlusMoreLabel() {
        val item1 = PurchaseOrderItemDto(
            itemId = 1,
            productId = 3190,
            name = "Nandini Pure Ghee 500ml",
            category = "Dairy",
            iconKey = "milk_bottle",
            quantity = 15
        )
        val item2 = PurchaseOrderItemDto(
            itemId = 2,
            productId = 3180,
            name = "Tata Sampann Unpolished Toor Dal 1kg",
            category = "Grocery",
            iconKey = "sheaf_of_rice",
            quantity = 40
        )
        val order = PurchaseOrderDto(
            orderNo = "PO-2026-001",
            totalItems = 2,
            totalUnits = 55,
            items = listOf(item1, item2)
        )

        assertEquals("Nandini Pure Ghee 500ml", order.cardProductName)
        assertEquals("Dairy", order.cardCategory)
        assertEquals("milk_bottle", order.cardIconKey)

        val totalItemsCount = order.totalItems ?: order.items?.size ?: 0
        val extraItemsCount = if (totalItemsCount > 1) totalItemsCount - 1 else maxOf(0, (order.items?.size ?: 1) - 1)
        assertEquals(1, extraItemsCount)

        val productTitle = if (extraItemsCount > 0 && order.cardProductName != "—") {
            "${order.cardProductName} + $extraItemsCount more"
        } else {
            order.cardProductName
        }
        assertEquals("Nandini Pure Ghee 500ml + 1 more", productTitle)
    }

    @Test
    fun testThreeItemOrder_showsFirstProductNamePlusTwoMore() {
        val item1 = PurchaseOrderItemDto(name = "Aashirvaad Shudh Chakki Atta 5kg", category = "Grocery", iconKey = "bowl_flour")
        val item2 = PurchaseOrderItemDto(name = "Fortune Sunlite Refined Sunflower Oil 1L", category = "Grocery", iconKey = "oil_drop")
        val item3 = PurchaseOrderItemDto(name = "Tata Salt Vacuum Evaporated 1kg", category = "Grocery", iconKey = "salt")

        val order = PurchaseOrderDto(
            orderNo = "PO-2026-002",
            totalItems = 3,
            totalUnits = 70,
            items = listOf(item1, item2, item3)
        )

        val totalItemsCount = order.totalItems ?: order.items?.size ?: 0
        val extraItemsCount = if (totalItemsCount > 1) totalItemsCount - 1 else maxOf(0, (order.items?.size ?: 1) - 1)
        assertEquals(2, extraItemsCount)

        val productTitle = "${order.cardProductName} + $extraItemsCount more"
        assertEquals("Aashirvaad Shudh Chakki Atta 5kg + 2 more", productTitle)
        assertEquals("bowl_flour", order.cardIconKey)
    }

    @Test
    fun testFallback_whenItemsEmpty_usesTopLevelSummaryFields() {
        val order = PurchaseOrderDto(
            orderNo = "PO-2026-099",
            totalItems = 2,
            totalUnits = 30,
            firstProductName = "Britannia Rusk Toast 300g",
            firstProductCategory = "Bakery",
            firstProductIcon = "bread",
            items = emptyList()
        )

        assertEquals("Britannia Rusk Toast 300g", order.cardProductName)
        assertEquals("Bakery", order.cardCategory)
        assertEquals("bread", order.cardIconKey)

        val totalItemsCount = order.totalItems ?: 0
        val extraItemsCount = if (totalItemsCount > 1) totalItemsCount - 1 else 0
        assertEquals(1, extraItemsCount)

        val productTitle = "${order.cardProductName} + $extraItemsCount more"
        assertEquals("Britannia Rusk Toast 300g + 1 more", productTitle)
    }

    @Test
    fun testFallback_whenItemsEmptyAndNoTopLevelFields_neverReturnsDashes() {
        val order = PurchaseOrderDto(
            orderNo = "PO-2026-100",
            totalItems = 1,
            totalUnits = 10,
            items = emptyList()
        )

        assertNotEquals("—", order.cardProductName)
        assertEquals("Order PO-2026-100", order.cardProductName)
    }

    @Test
    fun testDeserialization_fromBackendOrdersResponse() {
        val json = """
            {
                "status": true,
                "message": "Purchase orders retrieved successfully.",
                "data": {
                    "pagination": { "page": 1, "limit": 20, "total_orders": 1, "total_pages": 1 },
                    "orders": [
                        {
                            "id": 331,
                            "order_no": "PO-2026-002",
                            "status": "received",
                            "total_estimated_cost": 7380.0,
                            "total_items": 3,
                            "total_units": 70,
                            "first_product_name": "Aashirvaad Shudh Chakki Atta 5kg",
                            "first_product_category": "Grocery",
                            "first_product_icon": "bowl_flour",
                            "items": [
                                {
                                    "item_id": 1,
                                    "product_id": 3167,
                                    "name": "Aashirvaad Shudh Chakki Atta 5kg",
                                    "category": "Grocery",
                                    "icon_key": "bowl_flour",
                                    "quantity": 20
                                }
                            ]
                        }
                    ]
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, PurchaseOrdersListResponse::class.java)
        assertTrue(response.status == true)
        val order = response.data?.orders?.firstOrNull()
        assertNotNull(order)
        assertEquals("Aashirvaad Shudh Chakki Atta 5kg", order?.cardProductName)
        assertEquals("bowl_flour", order?.cardIconKey)
        assertEquals("Grocery", order?.cardCategory)
        assertEquals(3, order?.totalItems)
    }
}
