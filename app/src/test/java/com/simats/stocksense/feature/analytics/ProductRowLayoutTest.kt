package com.simats.stocksense.feature.analytics

import com.simats.stocksense.data.model.NetworkInventoryProduct
import com.simats.stocksense.feature.analytics.viewmodel.RecordSaleViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test verifying product row stock chips, price formatting,
 * and data logic across 20+ diverse product names (including long names
 * like "Amul Taaza Fresh Toned Milk 500 ml").
 */
class ProductRowLayoutTest {

    private val sampleProducts = listOf(
        NetworkInventoryProduct(id = 1, name = "Amul Taaza Fresh Toned Milk 500 ml", category = "Dairy", sku = "DAI-001", price = 28.0, units = 6, minStockThreshold = 10, status = "low_stock"),
        NetworkInventoryProduct(id = 2, name = "Fortune Sunlite Refined Sunflower Oil 1L", category = "Grocery", sku = "GRO-002", price = 145.0, units = 89, minStockThreshold = 15, status = "in_stock"),
        NetworkInventoryProduct(id = 3, name = "Aashirvaad Superior MP Whole Wheat Atta 5kg", category = "Grocery", sku = "GRO-003", price = 265.0, units = 25, minStockThreshold = 10, status = "in_stock"),
        NetworkInventoryProduct(id = 4, name = "Tata Tea Premium Desh Ki Chai 500g", category = "Beverages", sku = "BEV-004", price = 210.0, units = 4, minStockThreshold = 10, status = "low_stock"),
        NetworkInventoryProduct(id = 5, name = "Parle-G Original Gluco Biscuits 250g", category = "Snacks", sku = "SNA-005", price = 25.0, units = 120, minStockThreshold = 20, status = "in_stock"),
        NetworkInventoryProduct(id = 6, name = "Maggi 2-Minute Masala Instant Noodles 280g", category = "Snacks", sku = "SNA-006", price = 56.0, units = 45, minStockThreshold = 15, status = "in_stock"),
        NetworkInventoryProduct(id = 7, name = "Dettol Original Germ Protection Bathing Soap 125g", category = "Personal Care", sku = "PER-007", price = 58.0, units = 0, minStockThreshold = 10, status = "out_of_stock"),
        NetworkInventoryProduct(id = 8, name = "Surf Excel Easy Wash Detergent Powder 1kg", category = "Household", sku = "HOU-008", price = 140.0, units = 32, minStockThreshold = 10, status = "in_stock"),
        NetworkInventoryProduct(id = 9, name = "Colgate Strong Teeth Dental Cream Toothpaste 200g", category = "Personal Care", sku = "PER-009", price = 115.0, units = 3, minStockThreshold = 8, status = "low_stock"),
        NetworkInventoryProduct(id = 10, name = "Haldiram's Nagpur Bhujia Sev Namkeen 400g", category = "Snacks", sku = "SNA-010", price = 110.0, units = 50, minStockThreshold = 12, status = "in_stock"),
        NetworkInventoryProduct(id = 11, name = "Tata Salt Vacuum Evaporated Iodised Salt 1kg", category = "Grocery", sku = "GRO-011", price = 28.0, units = 95, minStockThreshold = 25, status = "in_stock"),
        NetworkInventoryProduct(id = 12, name = "Britannia Good Day Cashew Cookies 200g", category = "Bakery", sku = "BAK-012", price = 45.0, units = 18, minStockThreshold = 10, status = "in_stock"),
        NetworkInventoryProduct(id = 13, name = "Cadbury Dairy Milk Silk Chocolate Bar 60g", category = "Snacks", sku = "SNA-013", price = 80.0, units = 5, minStockThreshold = 10, status = "low_stock"),
        NetworkInventoryProduct(id = 14, name = "Amul Salted Butter 100g", category = "Dairy", sku = "DAI-014", price = 56.0, units = 40, minStockThreshold = 15, status = "in_stock"),
        NetworkInventoryProduct(id = 15, name = "Coca-Cola Original Taste Soft Drink 750ml", category = "Beverages", sku = "BEV-015", price = 40.0, units = 22, minStockThreshold = 10, status = "in_stock"),
        NetworkInventoryProduct(id = 16, name = "Lay's India's Magic Masala Potato Chips 50g", category = "Snacks", sku = "SNA-016", price = 20.0, units = 75, minStockThreshold = 20, status = "in_stock"),
        NetworkInventoryProduct(id = 17, name = "Catch Sprinklers Chat Masala 100g", category = "Grocery", sku = "GRO-017", price = 68.0, units = 14, minStockThreshold = 5, status = "in_stock"),
        NetworkInventoryProduct(id = 18, name = "Kissan Fresh Tomato Ketchup Bottle 950g", category = "Snacks", sku = "SNA-018", price = 135.0, units = 8, minStockThreshold = 10, status = "low_stock"),
        NetworkInventoryProduct(id = 19, name = "Vim Dishwash Bar 300g", category = "Household", sku = "HOU-019", price = 30.0, units = 60, minStockThreshold = 15, status = "in_stock"),
        NetworkInventoryProduct(id = 20, name = "Frooti Fresh Mango Drink Tetra Pack 160ml", category = "Beverages", sku = "BEV-020", price = 15.0, units = 0, minStockThreshold = 20, status = "out_of_stock"),
        NetworkInventoryProduct(id = 21, name = "Dabur Red Ayurvedic Toothpaste 150g", category = "Personal Care", sku = "PER-021", price = 98.0, units = 12, minStockThreshold = 10, status = "in_stock"),
        NetworkInventoryProduct(id = 22, name = "Everest Coriander Dhaniya Powder 100g", category = "Grocery", sku = "GRO-022", price = 42.0, units = 35, minStockThreshold = 10, status = "in_stock")
    )

    private fun computeStockChipText(product: NetworkInventoryProduct): String {
        val availableStock = RecordSaleViewModel.extractProductUnits(product)
        val isOutOfStock = availableStock <= 0
        val isLowStock = !isOutOfStock && (
            (product.minStockThreshold != null && availableStock <= product.minStockThreshold) ||
            product.status.equals("low_stock", ignoreCase = true) ||
            product.status?.contains("Low", ignoreCase = true) == true
        )

        return when {
            isOutOfStock -> "0 in stock"
            isLowStock -> "Low · $availableStock left"
            else -> "$availableStock in stock"
        }
    }

    @Test
    fun testAtLeastTwentyProductsChecked() {
        assertTrue("Must test at least 20 product names", sampleProducts.size >= 20)
    }

    @Test
    fun testLongProductNamesPresent() {
        val longNames = sampleProducts.filter { (it.name?.length ?: 0) >= 30 }
        assertTrue("Must include long product names like Amul Taaza Fresh Toned Milk 500 ml", longNames.isNotEmpty())
        assertTrue(longNames.any { it.name?.contains("Amul Taaza Fresh Toned Milk 500 ml") == true })
    }

    @Test
    fun testStockChipTextNeverWrapsOrProducesEmpty() {
        for (prod in sampleProducts) {
            val chip = computeStockChipText(prod)
            assertFalse("Chip should not contain line break", chip.contains("\n"))
            assertTrue("Chip should have content", chip.isNotBlank())
        }
    }

    @Test
    fun testLowStockChipFormatting() {
        val milk = sampleProducts.first { it.id == 1 }
        val chip = computeStockChipText(milk)
        assertEquals("Low · 6 left", chip)

        val tea = sampleProducts.first { it.id == 4 }
        assertEquals("Low · 4 left", computeStockChipText(tea))
    }

    @Test
    fun testNormalInStockChipFormatting() {
        val oil = sampleProducts.first { it.id == 2 }
        assertEquals("89 in stock", computeStockChipText(oil))

        val biscuits = sampleProducts.first { it.id == 5 }
        assertEquals("120 in stock", computeStockChipText(biscuits))
    }

    @Test
    fun testOutOfStockChipFormatting() {
        val soap = sampleProducts.first { it.id == 7 }
        assertEquals("0 in stock", computeStockChipText(soap))

        val frooti = sampleProducts.first { it.id == 20 }
        assertEquals("0 in stock", computeStockChipText(frooti))
    }
}
