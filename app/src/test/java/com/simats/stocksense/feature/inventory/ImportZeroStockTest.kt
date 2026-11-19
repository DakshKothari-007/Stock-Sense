package com.simats.stocksense.feature.inventory

import com.google.gson.Gson
import com.simats.stocksense.data.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Zero Stock Products in Import, Skip Invalid Rows, and Out of Stock creation.
 */
class ImportZeroStockTest {

    private val gson = Gson()

    @Test
    fun testNewProductWithZeroQuantity_isValid() {
        val json = """
            {
                "status": true,
                "message": "Validation complete: all 30 rows are ready for import.",
                "data": {
                    "rows_checked": 30,
                    "ready_rows": 30,
                    "errors_remaining": 0,
                    "ready_to_import": true,
                    "errors": [],
                    "stock_preview": [
                        {
                            "name": "Real Mango Juice 1 L",
                            "sku": "SKU-REAL-1234",
                            "current_units": 0,
                            "units_to_add": 0,
                            "new_units": 0,
                            "display_calculation": "0 + 0 = 0",
                            "is_new": true,
                            "is_out_of_stock": true,
                            "cost_price": 98.0,
                            "price": 110.0
                        }
                    ]
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, ImportValidateResponse::class.java)

        assertTrue(response.status == true)
        val data = response.data
        assertNotNull(data)
        assertEquals(30, data?.readyRows)
        assertEquals(0, data?.errorsRemaining)
        assertTrue(data?.readyToImport == true)

        val mangoPreview = data?.stockPreview?.firstOrNull { it.name?.contains("Mango") == true }
        assertNotNull(mangoPreview)
        assertEquals(0, mangoPreview?.newUnits)
        assertTrue(mangoPreview?.isOutOfStock == true)
        assertTrue(mangoPreview?.isNew == true)
    }

    @Test
    fun testExistingProductWithZeroQuantity_triggersExactErrorMessage() {
        val json = """
            {
                "status": true,
                "message": "Validation found 1 error(s) across rows.",
                "data": {
                    "rows_checked": 1,
                    "ready_rows": 0,
                    "errors_remaining": 1,
                    "ready_to_import": false,
                    "errors": [
                        {
                            "row_number": 2,
                            "field": "units",
                            "error_type": "QUANTITY_ZERO_EXISTING",
                            "error_badge": "ROW 2 · ZERO QUANTITY",
                            "product_name": "Amul Milk 500 ml",
                            "sku": "DAI-001",
                            "imported_value": "0",
                            "reason": "Quantity to add must be more than 0 for existing products",
                            "suggested_correction": 0,
                            "suggested_label": "Import with 0 units (out of stock)",
                            "actions": [
                                {
                                    "action": "set_zero",
                                    "label": "Import with 0 units (out of stock)",
                                    "value": 0
                                },
                                {
                                    "action": "skip_row",
                                    "label": "Skip this row",
                                    "value": "skip"
                                }
                            ]
                        }
                    ]
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, ImportValidateResponse::class.java)

        val error = response.data?.errors?.firstOrNull()
        assertNotNull(error)
        assertEquals("Quantity to add must be more than 0 for existing products", error?.reason)
        assertEquals("ROW 2 · ZERO QUANTITY", error?.errorBadge)
        assertEquals(0.0, (error?.suggestedCorrection as? Number)?.toDouble())
        assertEquals("Import with 0 units (out of stock)", error?.suggestedLabel)

        assertEquals(2, error?.actions?.size)
        assertEquals("set_zero", error?.actions?.get(0)?.action)
        assertEquals("skip_row", error?.actions?.get(1)?.action)
    }

    @Test
    fun testExecuteRequestWithSkipInvalidRows() {
        val request = ImportExecuteRequest(
            fileToken = "imp_123_test.csv",
            fileName = "stocksense_demo_products.csv",
            columnMapping = mapOf("name" to "Product Name", "units" to "Stock"),
            skipInvalidRows = true
        )

        val json = gson.toJson(request)
        assertTrue(json.contains("\"skip_invalid_rows\":true"))
    }

    @Test
    fun testExecuteResponseWith29of30ImportedAndOutOfStockProduct() {
        val json = """
            {
                "status": true,
                "message": "Your shelves are up to date. Import complete.",
                "data": {
                    "import_id": 105,
                    "file_name": "stocksense_demo_products.csv",
                    "products_updated": 0,
                    "products_added": 29,
                    "data_rows_imported": "29 of 30",
                    "data_rows_imported_count": 29,
                    "stock_added": "1420 units",
                    "stock_added_count": 1420,
                    "corrected_rows": 0,
                    "skipped_rows": 1,
                    "skipped_count": 1,
                    "corrected_skipped_text": "0 / 1 rows",
                    "total_active_products": 29,
                    "new_inventory_value": "₹1,24,500",
                    "new_inventory_value_raw": 124500.0,
                    "out_of_stock_products": [
                        {
                            "name": "Real Mango Juice 1 L",
                            "sku": "BEV-020",
                            "category": "Beverages",
                            "units": 0
                        }
                    ],
                    "out_of_stock_count": 1
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, ImportExecuteResponse::class.java)

        assertTrue(response.status == true)
        val data = response.data
        assertNotNull(data)
        assertEquals("29 of 30", data?.dataRowsImported)
        assertEquals(29, data?.dataRowsImportedCount)
        assertEquals(1, data?.skippedRows)
        assertEquals(1, data?.skippedCount)
        assertEquals(1, data?.outOfStockCount)

        val oos = data?.outOfStockProducts?.firstOrNull()
        assertNotNull(oos)
        assertEquals("Real Mango Juice 1 L", oos?.name)
        assertEquals(0, oos?.units)
    }
}
