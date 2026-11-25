package com.simats.stocksense.feature.dashboard

import com.google.gson.Gson
import com.simats.stocksense.data.model.DashboardAiPriority
import com.simats.stocksense.feature.dashboard.ui.parseProductId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AiPriorityProductTest {

    private val gson = Gson()

    @Test
    fun parseProductId_handlesIntLongDoubleAndString() {
        assertEquals(18, parseProductId(18))
        assertEquals(18, parseProductId(18L))
        // Gson deserializes numeric JSON values into Any? fields as Double
        assertEquals(18, parseProductId(18.0))
        assertEquals(18, parseProductId("18"))
        assertEquals(18, parseProductId(" 18 "))
        assertEquals(18, parseProductId("18.0"))
    }

    @Test
    fun parseProductId_returnsNullForMissingOrInvalidValues() {
        assertNull(parseProductId(null))
        assertNull(parseProductId(""))
        assertNull(parseProductId("   "))
        assertNull(parseProductId("abc"))
        assertNull(parseProductId(0))
        assertNull(parseProductId(0.0))
        assertNull(parseProductId(-4))
        assertNull(parseProductId(18.5))
        assertNull(parseProductId(Double.NaN))
    }

    @Test
    fun gsonDeserializedPriority_extractsProductIdAndSku() {
        val json = """
            {
                "type": "reorder",
                "title": "Reorder 20 Dove Soap units",
                "subtitle": "Current stock: 4 · Minimum: 12",
                "product_id": 42,
                "sku": "SOP-002"
            }
        """.trimIndent()

        val priority = gson.fromJson(json, DashboardAiPriority::class.java)
        assertEquals(42, parseProductId(priority.productId))
        assertEquals("SOP-002", priority.sku)
    }

    @Test
    fun gsonDeserializedPriority_missingBothIdAndSku_returnsNulls() {
        val json = """
            {
                "type": "reorder",
                "title": "Reorder needed",
                "product_id": null,
                "sku": "  "
            }
        """.trimIndent()

        val priority = gson.fromJson(json, DashboardAiPriority::class.java)
        assertNull(parseProductId(priority.productId))
        assertNull(priority.sku?.trim()?.takeIf { it.isNotBlank() })
    }
}
