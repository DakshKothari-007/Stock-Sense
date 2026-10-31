package com.simats.stocksense.data.repository

import com.google.gson.Gson
import com.simats.stocksense.core.network.ImportApiService
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

/**
 * Repository for Spreadsheet Bulk Import operations.
 */
class ImportRepository(
    private val apiService: ImportApiService = NetworkClient.createService(),
    private val gson: Gson = NetworkClient.gson
) {

    suspend fun uploadSpreadsheet(filePart: MultipartBody.Part): Result<ImportUploadData> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.uploadSpreadsheet(filePart)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == true && body.data != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(body?.message ?: "Failed to upload spreadsheet."))
                    }
                } else {
                    val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(mapThrowable(e))
            }
        }

    suspend fun previewImport(
        fileToken: String,
        columnMapping: Map<String, String?>? = null
    ): Result<ImportPreviewData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.previewImport(
                ImportPreviewRequest(
                    fileToken = fileToken,
                    columnMapping = columnMapping
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to preview spreadsheet."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(mapThrowable(e))
        }
    }

    suspend fun validateImport(
        fileToken: String,
        columnMapping: Map<String, String?>? = null,
        corrections: List<ImportCorrection>? = null
    ): Result<ImportValidateData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.validateImport(
                ImportValidateRequest(
                    fileToken = fileToken,
                    columnMapping = columnMapping,
                    corrections = corrections
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Validation failed."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(mapThrowable(e))
        }
    }

    suspend fun executeImport(
        fileToken: String,
        fileName: String? = null,
        columnMapping: Map<String, String?>? = null,
        corrections: List<ImportCorrection>? = null,
        confirmReplace: Boolean = false,
        idempotencyKey: String? = null
    ): Result<ImportExecuteData> = withContext(Dispatchers.IO) {
        try {
            val request = ImportExecuteRequest(
                fileToken = fileToken,
                fileName = fileName,
                columnMapping = columnMapping,
                corrections = corrections,
                confirmReplace = confirmReplace,
                idempotencyKey = idempotencyKey
            )
            val response = apiService.executeImport(
                request = request,
                idempotencyKey = idempotencyKey
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Import execution failed."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(mapThrowable(e))
        }
    }

    suspend fun getImportHistory(
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<ImportHistoryItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getImportHistory(limit = limit, offset = offset)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data.history ?: emptyList())
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to retrieve import history."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(mapThrowable(e))
        }
    }

    private fun mapThrowable(e: Throwable): Exception {
        return if (isTimeout(e)) {
            Exception("Connection timed out. Check your Wi-Fi and try again")
        } else if (e is Exception) {
            e
        } else {
            Exception(e.message, e)
        }
    }

    private fun isTimeout(e: Throwable): Boolean {
        var current: Throwable? = e
        while (current != null) {
            if (current is java.net.SocketTimeoutException ||
                current is java.io.InterruptedIOException ||
                current is java.net.ConnectException ||
                current.message?.contains("timeout", ignoreCase = true) == true ||
                current.message?.contains("timed out", ignoreCase = true) == true
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    private fun extractErrorMessage(errorBody: String?, fallbackCode: Int): String {
        if (fallbackCode == 408 || fallbackCode == 504) {
            return "Connection timed out. Check your Wi-Fi and try again"
        }
        if (errorBody.isNullOrBlank()) return "Server returned error code $fallbackCode"
        return try {
            val map = gson.fromJson(errorBody, Map::class.java)
            val msg = map["message"]?.toString() ?: errorBody
            if (msg.contains("timeout", ignoreCase = true) || msg.contains("timed out", ignoreCase = true)) {
                "Connection timed out. Check your Wi-Fi and try again"
            } else {
                msg
            }
        } catch (e: Exception) {
            if (errorBody.contains("timeout", ignoreCase = true) || errorBody.contains("timed out", ignoreCase = true)) {
                "Connection timed out. Check your Wi-Fi and try again"
            } else {
                errorBody
            }
        }
    }
}

