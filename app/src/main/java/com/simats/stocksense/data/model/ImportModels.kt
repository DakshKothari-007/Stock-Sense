package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data models for Spreadsheet Bulk Import module adhering strictly to API_DOCUMENT.md.
 * All response model fields are nullable with safe defaults to guarantee crash resilience.
 */

// 1. Upload Response
data class ImportUploadResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: ImportUploadData? = null
)

data class ImportUploadData(
    @SerializedName("file_token") val fileToken: String? = "",
    @SerializedName("file_name") val fileName: String? = "",
    @SerializedName("file_size") val fileSize: String? = "",
    @SerializedName("file_size_bytes") val fileSizeBytes: Long? = 0,
    @SerializedName("total_rows_detected") val totalRowsDetected: Int? = 0,
    @SerializedName("headers") val headers: List<String>? = emptyList(),
    @SerializedName("preview_rows") val previewRows: List<List<String>>? = emptyList(),
    @SerializedName("suggested_mapping") val suggestedMapping: Map<String, String?>? = null,
    @SerializedName("confidence") val confidence: Map<String, Double?>? = null,
    @SerializedName("matched_count") val matchedCount: Int? = 0,
    @SerializedName("total_columns") val totalColumns: Int? = 0,
    @SerializedName("required_matched") val requiredMatched: Boolean? = false,
    @SerializedName("is_saved") val isSaved: Boolean? = false
)

// 2. Preview Request & Response
data class ImportPreviewRequest(
    @SerializedName("file_token") val fileToken: String,
    @SerializedName("column_mapping") val columnMapping: Map<String, String?>? = null
)

data class ImportPreviewResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: ImportPreviewData? = null
)

data class ImportPreviewData(
    @SerializedName("mapped_columns_count") val mappedColumnsCount: Int? = 0,
    @SerializedName("matched_count") val matchedCount: Int? = 0,
    @SerializedName("total_columns") val totalColumns: Int? = 0,
    @SerializedName("required_matched") val requiredMatched: Boolean? = false,
    @SerializedName("is_saved") val isSaved: Boolean? = false,
    @SerializedName("confidence") val confidence: Map<String, Double?>? = null,
    @SerializedName("mappings") val mappings: Map<String, String?>? = emptyMap(),
    @SerializedName("available_headers") val availableHeaders: List<String>? = emptyList(),
    @SerializedName("headers") val headers: List<String>? = emptyList(),
    @SerializedName("total_rows") val totalRows: Int? = 0,
    @SerializedName("preview_rows") val previewRows: List<ImportPreviewRow>? = emptyList()
) {
    val allHeaders: List<String>
        get() = (if (!availableHeaders.isNullOrEmpty()) availableHeaders else headers)?.filterNotNull() ?: emptyList()
}

data class ImportPreviewRow(
    @SerializedName("row_number") val rowNumber: Int? = 0,
    @SerializedName("name") val name: String? = "",
    @SerializedName("sku") val sku: String? = "",
    @SerializedName("skuWithSellPrice") val skuWithSellPrice: String? = "",
    @SerializedName("unitsToAdd") val unitsToAdd: String? = "0",
    @SerializedName("costPrice") val costPrice: String? = "₹0",
    @SerializedName("sellingPrice") val sellingPrice: String? = "₹0",
    @SerializedName("isHighlighted") val isHighlighted: Boolean? = false,
    @SerializedName("isExisting") val isExisting: Boolean? = false,
    @SerializedName("currentUnits") val currentUnits: Int? = 0
)

// 3. Validate Request & Response
data class ImportCorrection(
    @SerializedName("row_number") val rowNumber: Int,
    @SerializedName("field") val field: String,
    @SerializedName("value") val value: Any
)

data class ImportValidateRequest(
    @SerializedName("file_token") val fileToken: String,
    @SerializedName("column_mapping") val columnMapping: Map<String, String?>? = null,
    @SerializedName("corrections") val corrections: List<ImportCorrection>? = null
)

data class ImportValidateResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: ImportValidateData? = null
)

data class ImportValidateData(
    @SerializedName("rows_checked") val rowsChecked: Int? = 0,
    @SerializedName("ready_rows") val readyRows: Int? = 0,
    @SerializedName("errors_remaining") val errorsRemaining: Int? = 0,
    @SerializedName("ready_to_import") val readyToImport: Boolean? = false,
    @SerializedName("errors") val errors: List<ImportValidationError>? = emptyList(),
    @SerializedName("summary") val summary: ImportSummary? = null,
    @SerializedName("stock_preview") val stockPreview: List<ImportStockPreviewItem>? = emptyList()
)

data class ImportValidationError(
    @SerializedName("row_number") val rowNumber: Int? = 0,
    @SerializedName("field") val field: String? = "",
    @SerializedName("error_type") val errorType: String? = "",
    @SerializedName("error_badge") val errorBadge: String? = "",
    @SerializedName("product_name") val productName: String? = "",
    @SerializedName("sku") val sku: String? = "",
    @SerializedName("imported_value") val importedValue: String? = "",
    @SerializedName("reason") val reason: String? = "",
    @SerializedName("suggested_correction") val suggestedCorrection: Any? = null,
    @SerializedName("suggested_label") val suggestedLabel: String? = null
)

data class ImportSummary(
    @SerializedName("existing_products_to_update") val existingProductsToUpdate: Int? = 0,
    @SerializedName("new_products_to_create") val newProductsToCreate: Int? = 0,
    @SerializedName("total_stock_to_add") val totalStockToAdd: Int? = 0,
    @SerializedName("total_stock_to_add_formatted") val totalStockToAddFormatted: String? = "0 units",
    @SerializedName("inventory_value_to_add") val inventoryValueToAdd: Double? = 0.0,
    @SerializedName("inventory_value_to_add_formatted") val inventoryValueToAddFormatted: String? = "₹0"
)

data class ImportStockPreviewItem(
    @SerializedName("name") val name: String? = "",
    @SerializedName("sku") val sku: String? = "",
    @SerializedName("current_units") val currentUnits: Int? = 0,
    @SerializedName("units_to_add") val unitsToAdd: Int? = 0,
    @SerializedName("new_units") val newUnits: Int? = 0,
    @SerializedName("display_calculation") val displayCalculation: String? = "",
    @SerializedName("is_new") val isNew: Boolean? = false,
    @SerializedName("cost_price") val costPrice: Double? = 0.0,
    @SerializedName("price") val price: Double? = 0.0
)

// 4. Execute Request & Response
data class ImportExecuteRequest(
    @SerializedName("file_token") val fileToken: String,
    @SerializedName("file_name") val fileName: String? = null,
    @SerializedName("column_mapping") val columnMapping: Map<String, String?>? = null,
    @SerializedName("corrections") val corrections: List<ImportCorrection>? = null,
    @SerializedName("confirm_replace") val confirmReplace: Boolean = false
)

data class ImportExecuteResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: ImportExecuteData? = null
)

data class ImportExecuteData(
    @SerializedName("import_id") val importId: Int? = 0,
    @SerializedName("file_name") val fileName: String? = "",
    @SerializedName("products_updated") val productsUpdated: Int? = 0,
    @SerializedName("products_added") val productsAdded: Int? = 0,
    @SerializedName("data_rows_imported") val dataRowsImported: String? = "0 of 0",
    @SerializedName("data_rows_imported_count") val dataRowsImportedCount: Int? = 0,
    @SerializedName("stock_added") val stockAdded: String? = "0 units",
    @SerializedName("stock_added_count") val stockAddedCount: Int? = 0,
    @SerializedName("corrected_rows") val correctedRows: Int? = 0,
    @SerializedName("skipped_rows") val skippedRows: Int? = 0,
    @SerializedName("corrected_skipped_text") val correctedSkippedText: String? = "0 / 0 rows",
    @SerializedName("total_active_products") val totalActiveProducts: Int? = 0,
    @SerializedName("new_inventory_value") val newInventoryValue: String? = "₹0",
    @SerializedName("new_inventory_value_raw") val newInventoryValueRaw: Double? = 0.0
)

// 5. History Response
data class ImportHistoryResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: ImportHistoryData? = null
)

data class ImportHistoryData(
    @SerializedName("total_imports") val totalImports: Int? = 0,
    @SerializedName("count") val count: Int? = 0,
    @SerializedName("limit") val limit: Int? = 20,
    @SerializedName("offset") val offset: Int? = 0,
    @SerializedName("history") val history: List<ImportHistoryItem>? = emptyList()
)

data class ImportHistoryItem(
    @SerializedName("id") val id: Int? = 0,
    @SerializedName("file_name") val fileName: String? = "",
    @SerializedName("file_size") val fileSize: String? = "",
    @SerializedName("file_size_bytes") val fileSizeBytes: Long? = 0,
    @SerializedName("total_rows") val totalRows: Int? = 0,
    @SerializedName("success_count") val successCount: Int? = 0,
    @SerializedName("failed_count") val failedCount: Int? = 0,
    @SerializedName("status") val status: String? = "",
    @SerializedName("created_at") val createdAt: String? = ""
)
