package com.simats.stocksense.feature.dashboard

import com.simats.stocksense.data.model.NotificationItem
import com.simats.stocksense.feature.inventory.icon.ProductIconMatcher
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying:
 * 1. NotificationItem model supports product_id, product_name, category, and icon_key.
 * 2. Product-related notifications with icon_key resolve to the explicit icon.
 * 3. Legacy/old product notifications without icon_key fall back to ProductIconMatcher using product name.
 * 4. Non-product notifications have null product fields.
 */
class NotificationProductIconTest {

    @Test
    fun testProductNotificationWithIconKey() {
        val item = NotificationItem(
            key = "low_stock:101",
            type = "low_stock",
            title = "Amul Taaza Milk 500ml is running low",
            body = "Only 2 units left.",
            productId = 101,
            productName = "Amul Taaza Milk 500ml",
            category = "Dairy",
            iconKey = "milk"
        )

        assertEquals(101, item.productId)
        assertEquals("Amul Taaza Milk 500ml", item.productName)
        assertEquals("Dairy", item.category)
        assertEquals("milk", item.iconKey)

        // Verify ProductIconMatcher resolves drawable using explicit icon_key
        val drawableRes = ProductIconMatcher.resolveDrawableRes(
            name = item.productName,
            category = item.category,
            savedIconKey = item.iconKey
        )
        assertNotEquals(0, drawableRes)
    }

    @Test
    fun testLegacyNotificationWithoutIconKeyFallsBackToMatcher() {
        // Old notification where icon_key is null
        val item = NotificationItem(
            key = "low_stock:102",
            type = "low_stock",
            title = "Aashirvaad Shudh Chakki Atta 5kg is running low",
            body = "Only 1 unit left.",
            productId = 102,
            productName = "Aashirvaad Shudh Chakki Atta 5kg",
            category = "Grocery",
            iconKey = null
        )

        assertNull(item.iconKey)

        // When iconKey is null, ProductIconMatcher should resolve by product name ('atta' / 'flour' rule)
        val drawableRes = ProductIconMatcher.resolveDrawableRes(
            name = item.productName,
            category = item.category,
            savedIconKey = null
        )
        assertNotEquals("Should resolve a valid drawable fallback from product name", 0, drawableRes)
    }

    @Test
    fun testNonProductNotificationHasNullProductFields() {
        val salesItem = NotificationItem(
            key = "sales:2026-11-18",
            type = "sales",
            title = "Sales summary so far today",
            body = "₹12,400 in sales.",
            productId = null,
            productName = null,
            category = null,
            iconKey = null
        )

        assertNull(salesItem.productId)
        assertNull(salesItem.productName)
        assertNull(salesItem.category)
        assertNull(salesItem.iconKey)

        val importItem = NotificationItem(
            key = "import:5",
            type = "import",
            title = "Inventory import completed",
            body = "50 items processed.",
            productId = null,
            productName = null,
            category = null,
            iconKey = null
        )

        assertNull(importItem.productId)
        assertNull(importItem.productName)
        assertNull(importItem.category)
        assertNull(importItem.iconKey)
    }

    @Test
    fun testProductAvatarResolutionForVariousCategories() {
        // Biscuits / Cookies
        val biscuitRes = ProductIconMatcher.resolveDrawableRes("Parle-G Gold Biscuits 1kg", "Biscuits", "cookie")
        assertNotEquals(0, biscuitRes)

        // Tea / Beverages
        val teaRes = ProductIconMatcher.resolveDrawableRes("Tata Tea Premium 500g", "Beverages", "tea")
        assertNotEquals(0, teaRes)

        // Shampoo / Personal Care
        val shampooRes = ProductIconMatcher.resolveDrawableRes("Head & Shoulders Shampoo 180ml", "Personal Care", "shampoo")
        assertNotEquals(0, shampooRes)
    }
}
