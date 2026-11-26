package com.simats.stocksense.feature.inventory.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.simats.stocksense.data.repository.InventoryRepository
import com.simats.stocksense.data.repository.UnauthorizedException
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ExportErrorResult(
    val message: String,
    val isSessionExpired: Boolean = false
)

data class SanitizedExportFileName(
    val baseName: String,
    val fileName: String,
    val charactersReplaced: Boolean,
    val usedDefault: Boolean
)

class ExportCsvException(val error: ExportErrorResult) : Exception(error.message)

/**
 * Helper for CSV export file naming, sanitization, error translation, cache storage, and FileProvider sharing.
 */
object ProductCsvExportHelper {

    const val MAX_EXPORT_NAME_LENGTH = 60

    private val gson = Gson()
    private val quotedFilenameRegex = Regex("""filename\*?=\s*(?:UTF-8'')?\"?([^\";\r\n]+)\"?""", RegexOption.IGNORE_CASE)

    /**
     * Returns the default editable export base name (without .csv), using local timezone date.
     */
    fun defaultExportBaseName(
        date: Date = Date(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).apply {
            this.timeZone = timeZone
        }
        return "stocksense-products-${dateFormat.format(date)}"
    }

    /**
     * Sanitizes a user-chosen CSV file name according to filename safety rules:
     * - Trims spaces; blank, ".", "..", or dot-only names fall back to default name.
     * - Strips a trailing ".csv" (case-insensitive) so it is not doubled.
     * - Allows Unicode letters (plus Hindi/Tamil combining marks), digits, space, '-', '_';
     *   replaces other characters with '_'.
     * - Caps base name length at 60 characters (not counting ".csv").
     */
    fun sanitizeExportFileName(
        rawInput: String?,
        date: Date = Date(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): SanitizedExportFileName {
        val defaultBase = defaultExportBaseName(date = date, timeZone = timeZone)
        var working = rawInput?.trim().orEmpty()

        if (isBlockedOrEmptyName(working)) {
            return SanitizedExportFileName(
                baseName = defaultBase,
                fileName = "$defaultBase.csv",
                charactersReplaced = false,
                usedDefault = true
            )
        }

        if (working.lowercase(Locale.ENGLISH).endsWith(".csv")) {
            working = working.dropLast(4).trimEnd()
        }

        if (isBlockedOrEmptyName(working)) {
            return SanitizedExportFileName(
                baseName = defaultBase,
                fileName = "$defaultBase.csv",
                charactersReplaced = false,
                usedDefault = true
            )
        }

        var replaced = false
        val sb = StringBuilder(working.length)
        var i = 0
        while (i < working.length) {
            val cp = working.codePointAt(i)
            val charCount = Character.charCount(cp)
            if (isAllowedExportCodePoint(cp)) {
                sb.appendCodePoint(cp)
            } else {
                sb.append('_')
                replaced = true
            }
            i += charCount
        }

        var sanitized = sb.toString().trim()
        if (sanitized.length > MAX_EXPORT_NAME_LENGTH) {
            sanitized = truncateSafe(sanitized, MAX_EXPORT_NAME_LENGTH).trimEnd()
        }

        if (isBlockedOrEmptyName(sanitized)) {
            return SanitizedExportFileName(
                baseName = defaultBase,
                fileName = "$defaultBase.csv",
                charactersReplaced = replaced,
                usedDefault = true
            )
        }

        return SanitizedExportFileName(
            baseName = sanitized,
            fileName = "$sanitized.csv",
            charactersReplaced = replaced,
            usedDefault = false
        )
    }

    /**
     * Resolves a safe export filename from Content-Disposition or defaults to stocksense_products_YYYYMMDD.csv.
     */
    fun buildExportFileName(
        contentDisposition: String? = null,
        date: Date = Date(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val candidate = contentDisposition
            ?.let { quotedFilenameRegex.find(it)?.groupValues?.getOrNull(1) }
            ?.trim()
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            ?.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            ?.trim('_', '.')

        if (!candidate.isNullOrBlank() && candidate.lowercase(Locale.ENGLISH).endsWith(".csv") && candidate.length > 4) {
            return candidate
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).apply {
            this.timeZone = timeZone
        }
        return "stocksense_products_${dateFormat.format(date)}.csv"
    }

    /**
     * Maps HTTP codes, JSON error payloads, and network throwables to user-friendly export errors.
     */
    fun mapExportError(
        httpCode: Int? = null,
        errorBody: String? = null,
        throwable: Throwable? = null
    ): ExportErrorResult {
        if (throwable is ExportCsvException) {
            return throwable.error
        }

        val serverMsg = extractServerMessage(errorBody)

        if (throwable is UnauthorizedException || httpCode == 401) {
            return ExportErrorResult(
                message = "Your session has expired. Please sign in again.",
                isSessionExpired = true
            )
        }

        if (httpCode == 429 ||
            serverMsg?.contains("too many", ignoreCase = true) == true ||
            serverMsg?.contains("rate limit", ignoreCase = true) == true
        ) {
            return ExportErrorResult(
                message = "Too many exports right now. Please try again later."
            )
        }

        if (httpCode == 404 ||
            serverMsg?.contains("no products to export", ignoreCase = true) == true ||
            serverMsg?.contains("no products", ignoreCase = true) == true
        ) {
            return ExportErrorResult(
                message = "No products to export. Add products to your inventory first."
            )
        }

        when (throwable) {
            is UnknownHostException, is ConnectException -> {
                return ExportErrorResult("No internet connection. Please check your connection and try again.")
            }
            is SocketTimeoutException -> {
                return ExportErrorResult("Connection timed out. Please check your connection and try again.")
            }
            is IOException -> {
                return ExportErrorResult("Could not connect to server. Please check your internet connection and try again.")
            }
        }

        if (!serverMsg.isNullOrBlank()) {
            return ExportErrorResult(serverMsg)
        }

        if (httpCode != null) {
            return ExportErrorResult("Could not export products (error $httpCode). Please try again.")
        }

        val fallback = throwable?.message?.takeIf { it.isNotBlank() }
            ?: "Could not export products. Please try again."
        return ExportErrorResult(fallback)
    }

    /**
     * Writes CSV bytes to the app cache/exports folder, overwriting any existing file with the same name.
     */
    fun writeCsvToCache(context: Context, fileName: String, csvBytes: ByteArray): File {
        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
        }
        val safeName = sanitizeExportFileName(fileName).fileName
        val targetFile = File(exportsDir, safeName)
        targetFile.writeBytes(csvBytes)
        return targetFile
    }

    /**
     * Saves the downloaded CSV bytes into cache and opens the system share sheet via FileProvider.
     */
    fun saveAndShareCsv(context: Context, fileName: String, csvBytes: ByteArray): Result<Uri> {
        return try {
            val csvFile = writeCsvToCache(context, fileName, csvBytes)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "StockSense Products (${csvFile.name})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export products (CSV)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(
                ExportCsvException(
                    ExportErrorResult("Unable to open share sheet: ${e.message ?: "please try again"}")
                )
            )
        }
    }

    /**
     * Downloads the product catalog CSV from the backend and shares it with the chosen file name.
     */
    suspend fun downloadAndShare(
        context: Context,
        inventoryRepository: InventoryRepository,
        chosenFileName: String? = null
    ): Result<Unit> {
        val resolvedName = sanitizeExportFileName(chosenFileName).fileName
        val downloadResult = inventoryRepository.exportProductsCsv()
        val exported = downloadResult.getOrElse { err ->
            return Result.failure(err)
        }
        return saveAndShareCsv(
            context = context,
            fileName = if (chosenFileName != null) resolvedName else exported.fileName,
            csvBytes = exported.bytes
        ).map { }
    }

    private fun isBlockedOrEmptyName(value: String): Boolean {
        return value.isEmpty() || value == "." || value == ".." || value.all { it == '.' }
    }

    private fun isAllowedExportCodePoint(cp: Int): Boolean {
        if (cp == ' '.code || cp == '-'.code || cp == '_'.code) return true
        if (Character.isISOControl(cp)) return false
        val type = Character.getType(cp)
        return Character.isLetterOrDigit(cp) ||
            type == Character.NON_SPACING_MARK.toInt() ||
            type == Character.COMBINING_SPACING_MARK.toInt()
    }

    private fun truncateSafe(value: String, maxChars: Int): String {
        if (value.length <= maxChars) return value
        var end = maxChars
        if (end > 0 && Character.isHighSurrogate(value[end - 1])) {
            end--
        }
        return value.substring(0, end)
    }

    private fun extractServerMessage(errorBody: String?): String? {
        val raw = errorBody?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return try {
            val map = gson.fromJson(raw, Map::class.java)
            map["message"]?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            if (raw.startsWith("<")) null else raw
        }
    }
}
