package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ImportApiService {

    @Multipart
    @POST("import/upload.php")
    suspend fun uploadSpreadsheet(
        @Part file: MultipartBody.Part
    ): Response<ImportUploadResponse>

    @POST("import/preview.php")
    suspend fun previewImport(
        @Body request: ImportPreviewRequest
    ): Response<ImportPreviewResponse>

    @POST("import/validate.php")
    suspend fun validateImport(
        @Body request: ImportValidateRequest
    ): Response<ImportValidateResponse>

    @POST("import/execute.php")
    suspend fun executeImport(
        @Body request: ImportExecuteRequest
    ): Response<ImportExecuteResponse>

    @GET("import/get_history.php")
    suspend fun getImportHistory(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ImportHistoryResponse>
}

