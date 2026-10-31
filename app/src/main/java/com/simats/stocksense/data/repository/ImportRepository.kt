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
                Result.failure(e)
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
            Result.failure(e)
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
            Result.failure(e)
        }
    }

    suspend fun executeImport(
        fileToken: String,
        fileName: String? = null,
        columnMapping: Map<String, String?>? = null,
        corrections: List<ImportCorrection>? = null,
        confirmReplace: Boolean = false
    ): Result<ImportExecuteData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.executeImport(
                ImportExecuteRequest(
                    fileToken = fileToken,
                    fileName = fileName,
                    columnMapping = columnMapping,
                    corrections = corrections,
                    confirmReplace = confirmReplace
                )
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
            Result.failure(e)
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
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(errorBody: String?, fallbackCode: Int): String {
        if (errorBody.isNullOrBlank()) return "Server returned error code $fallbackCode"
        return try {
            val map = gson.fromJson(errorBody, Map::class.java)
            map["message"]?.toString() ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}

