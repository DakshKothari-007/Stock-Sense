package com.simats.stocksense.feature.inventory.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.data.model.*
import com.simats.stocksense.data.repository.ImportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class ImportUiState(
    val selectedUri: Uri? = null,
    val selectedFileName: String? = null,
    val selectedFileSize: String? = null,
    val fileToken: String? = null,
    val columnMapping: Map<String, String?>? = null,
    val corrections: List<ImportCorrection> = emptyList(),
    val appliedCorrectionSummary: String? = null,

    // Upload & Preview state
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    val previewData: ImportPreviewData? = null,

    // Validation state
    val isValidating: Boolean = false,
    val validationError: String? = null,
    val validationData: ImportValidateData? = null,

    // Execution state
    val isExecuting: Boolean = false,
    val executeError: String? = null,
    val executionResult: ImportExecuteData? = null,

    // History state
    val historyList: List<ImportHistoryItem> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val historyError: String? = null
)

class ImportViewModel(
    private val repository: ImportRepository = ImportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    // Random idempotency key per import attempt; preserved across retries of the same attempt
    private var activeIdempotencyKey: String = java.util.UUID.randomUUID().toString()

    init {
        fetchHistory()
    }

    fun setSelectedFile(uri: Uri?, name: String?, sizeText: String?) {
        activeIdempotencyKey = java.util.UUID.randomUUID().toString()
        _uiState.update {
            it.copy(
                selectedUri = uri,
                selectedFileName = name,
                selectedFileSize = sizeText,
                uploadError = null
            )
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHistory = true, historyError = null) }
            val result = repository.getImportHistory(limit = 20, offset = 0)
            result.fold(
                onSuccess = { history ->
                    _uiState.update {
                        it.copy(
                            historyList = history,
                            isLoadingHistory = false,
                            historyError = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingHistory = false,
                            historyError = error.message ?: "Failed to load import history."
                        )
                    }
                }
            )
        }
    }

    fun uploadAndPreview(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uri = _uiState.value.selectedUri
        if (uri == null) {
            val err = "Please tap to select a file first."
            _uiState.update { it.copy(uploadError = err) }
            onError(err)
            return
        }

        val fileName = _uiState.value.selectedFileName ?: "stock_file.xlsx"
        val lowerName = fileName.lowercase()
        if (!lowerName.endsWith(".csv") && !lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls")) {
            val err = "Invalid file format. Only .csv, .xlsx, and .xls files are supported."
            _uiState.update { it.copy(uploadError = err) }
            onError(err)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadError = null) }
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() } ?: ByteArray(0)

                if (bytes.isEmpty()) {
                    val err = "Spreadsheet contains no data or is empty."
                    _uiState.update { it.copy(isUploading = false, uploadError = err) }
                    onError(err)
                    return@launch
                }

                if (bytes.size > 5 * 1024 * 1024) {
                    val sizeMb = String.format("%.2f", bytes.size / (1024.0 * 1024.0))
                    val err = "File size exceeds the 5 MB limit. Selected file size: $sizeMb MB."
                    _uiState.update { it.copy(isUploading = false, uploadError = err) }
                    onError(err)
                    return@launch
                }

                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                val mediaType = mimeType.toMediaTypeOrNull()
                val requestBody = bytes.toRequestBody(mediaType)
                val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)

                val uploadResult = repository.uploadSpreadsheet(filePart)
                uploadResult.fold(
                    onSuccess = { uploadData ->
                        val token = uploadData.fileToken.orEmpty()
                        if (token.isBlank()) {
                            val err = "Upload failed: file token missing from response."
                            _uiState.update { it.copy(isUploading = false, uploadError = err) }
                            onError(err)
                            return@fold
                        }
                        val mapping = uploadData.suggestedMapping
                        val previewResult = repository.previewImport(token, mapping)
                        previewResult.fold(
                            onSuccess = { previewData ->
                                val activeMapping = previewData.mappings?.takeIf { !it.isNullOrEmpty() }
                                    ?: mapping
                                    ?: emptyMap()

                                Log.d("ImportMapping", "upload.php auto-detected mapping: $mapping")
                                Log.d("ImportMapping", "preview.php resolved mapping: ${previewData.mappings}")

                                _uiState.update {
                                    it.copy(
                                        fileToken = token,
                                        columnMapping = activeMapping,
                                        previewData = previewData,
                                        isUploading = false,
                                        uploadError = null,
                                        corrections = emptyList(),
                                        appliedCorrectionSummary = null
                                    )
                                }
                                onSuccess()
                            },
                            onFailure = { previewErr ->
                                val msg = previewErr.message ?: "Failed to generate preview."
                                _uiState.update { it.copy(isUploading = false, uploadError = msg) }
                                onError(msg)
                            }
                        )
                    },
                    onFailure = { uploadErr ->
                        val msg = uploadErr.message ?: "Failed to upload file."
                        _uiState.update { it.copy(isUploading = false, uploadError = msg) }
                        onError(msg)
                    }
                )
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to process file."
                _uiState.update { it.copy(isUploading = false, uploadError = msg) }
                onError(msg)
            }
        }
    }

    fun updateColumnMapping(field: String, selectedHeader: String?) {
        val current = (_uiState.value.columnMapping ?: _uiState.value.previewData?.mappings ?: emptyMap()).toMutableMap()
        if (selectedHeader.isNullOrBlank() || selectedHeader == "Not Mapped") {
            current.remove(field)
        } else {
            current[field] = selectedHeader
        }

        _uiState.update { it.copy(columnMapping = current, validationError = null) }

        val token = _uiState.value.fileToken ?: return
        viewModelScope.launch {
            val previewResult = repository.previewImport(token, current)
            previewResult.onSuccess { newPreview ->
                _uiState.update { it.copy(previewData = newPreview) }
            }
        }
    }

    fun validateImport(
        onSuccess: (readyToImport: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val token = _uiState.value.fileToken
        if (token.isNullOrBlank()) {
            val err = "No active upload session. Please re-upload your file."
            _uiState.update { it.copy(validationError = err) }
            onError(err)
            return
        }

        val activeMapping = _uiState.value.columnMapping ?: _uiState.value.previewData?.mappings ?: emptyMap()
        Log.d("ImportMapping", "validate.php sending column_mapping: $activeMapping")

        // Block import only if required fields (name, quantity) are not mapped
        val nameCol = activeMapping["name"]
        val unitsCol = activeMapping["units"]

        if (nameCol.isNullOrBlank() || unitsCol.isNullOrBlank()) {
            val missing = mutableListOf<String>()
            if (nameCol.isNullOrBlank()) missing.add("Product Name")
            if (unitsCol.isNullOrBlank()) missing.add("Quantity")
            val err = "Validation blocked: Please map ${missing.joinToString(", ")} before continuing."
            _uiState.update { it.copy(validationError = err) }
            onError(err)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isValidating = true, validationError = null) }
            val result = repository.validateImport(
                fileToken = token,
                columnMapping = activeMapping,
                corrections = _uiState.value.corrections.takeIf { it.isNotEmpty() }
            )
            result.fold(
                onSuccess = { validateData ->
                    _uiState.update {
                        it.copy(
                            validationData = validateData,
                            isValidating = false,
                            validationError = null
                        )
                    }
                    onSuccess(validateData.readyToImport ?: false)
                },
                onFailure = { error ->
                    val msg = error.message ?: "Validation failed."
                    _uiState.update { it.copy(isValidating = false, validationError = msg) }
                    onError(msg)
                }
            )
        }
    }

    fun applyCorrection(
        error: ImportValidationError,
        onSuccess: (readyToImport: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val correctionValue = error.suggestedCorrection ?: return
        val newCorrection = ImportCorrection(
            rowNumber = error.rowNumber ?: 0,
            field = error.field.orEmpty(),
            value = correctionValue
        )
        val updatedCorrections = _uiState.value.corrections.filterNot {
            it.rowNumber == error.rowNumber && it.field == error.field
        } + newCorrection

        val summaryText = "Row ${error.rowNumber}: ${error.productName} ${error.field} changed from \"${error.importedValue}\" to ${error.suggestedLabel ?: correctionValue}."

        _uiState.update {
            it.copy(
                corrections = updatedCorrections,
                appliedCorrectionSummary = summaryText
            )
        }

        validateImport(onSuccess, onError)
    }

    fun executeImport(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Double-tap prevention guard
        if (_uiState.value.isExecuting) return

        val token = _uiState.value.fileToken
        if (token.isNullOrBlank()) {
            val err = "No active upload session. Please re-upload your file."
            _uiState.update { it.copy(executeError = err) }
            onError(err)
            return
        }

        val activeMapping = _uiState.value.columnMapping ?: _uiState.value.previewData?.mappings ?: emptyMap()
        Log.d("ImportMapping", "execute.php sending column_mapping: $activeMapping")

        // Block import only if required fields (name, quantity) are not mapped
        val nameCol = activeMapping["name"]
        val unitsCol = activeMapping["units"]

        if (nameCol.isNullOrBlank() || unitsCol.isNullOrBlank()) {
            val missing = mutableListOf<String>()
            if (nameCol.isNullOrBlank()) missing.add("Product Name")
            if (unitsCol.isNullOrBlank()) missing.add("Quantity")
            val err = "Import blocked: Please map ${missing.joinToString(", ")} before importing."
            _uiState.update { it.copy(executeError = err) }
            onError(err)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true, executeError = null) }
            val result = repository.executeImport(
                fileToken = token,
                fileName = _uiState.value.selectedFileName,
                columnMapping = activeMapping,
                corrections = _uiState.value.corrections.takeIf { it.isNotEmpty() },
                confirmReplace = false,
                idempotencyKey = activeIdempotencyKey
            )
            result.fold(
                onSuccess = { execData ->
                    // On successful execution, generate a fresh idempotency key for any future attempt
                    activeIdempotencyKey = java.util.UUID.randomUUID().toString()
                    _uiState.update {
                        it.copy(
                            executionResult = execData,
                            isExecuting = false,
                            executeError = null
                        )
                    }
                    fetchHistory()
                    onSuccess()
                },
                onFailure = { error ->
                    // Preserves activeIdempotencyKey across retries so re-executing ignores duplicate attempts
                    val msg = error.message ?: "Failed to execute import."
                    _uiState.update { it.copy(isExecuting = false, executeError = msg) }
                    onError(msg)
                }
            )
        }
    }

    fun clearSession() {
        activeIdempotencyKey = java.util.UUID.randomUUID().toString()
        _uiState.update {
            ImportUiState(
                historyList = it.historyList,
                isLoadingHistory = it.isLoadingHistory,
                historyError = it.historyError
            )
        }
    }
}
