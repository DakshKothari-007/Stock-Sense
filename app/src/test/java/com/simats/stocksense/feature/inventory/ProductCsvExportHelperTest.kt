package com.simats.stocksense.feature.inventory

import com.simats.stocksense.data.repository.UnauthorizedException
import com.simats.stocksense.feature.inventory.util.ProductCsvExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Calendar
import java.util.TimeZone

class ProductCsvExportHelperTest {

    @Test
    fun buildExportFileName_usesContentDispositionWhenValidCsv() {
        val name = ProductCsvExportHelper.buildExportFileName(
            contentDisposition = """attachment; filename="stocksense_products_20261126.csv""""
        )
        assertEquals("stocksense_products_20261126.csv", name)
    }

    @Test
    fun buildExportFileName_stripsPathTraversalAndHandlesUtf8Filename() {
        val name = ProductCsvExportHelper.buildExportFileName(
            contentDisposition = "attachment; filename*=UTF-8''../../secret/stocksense_products_20261126.csv"
        )
        assertEquals("stocksense_products_20261126.csv", name)
    }

    @Test
    fun buildExportFileName_fallsBackToFormattedDateWhenMissingOrNonCsv() {
        val ist = TimeZone.getTimeZone("Asia/Kolkata")
        // 2026-11-25 18:45:00 UTC == 2026-11-26 00:15:00 IST
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.NOVEMBER, 25, 18, 45, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fixedDate = cal.time

        assertEquals(
            "stocksense_products_20261126.csv",
            ProductCsvExportHelper.buildExportFileName(
                contentDisposition = null,
                date = fixedDate,
                timeZone = ist
            )
        )
        assertEquals(
            "stocksense_products_20261126.csv",
            ProductCsvExportHelper.buildExportFileName(
                contentDisposition = """attachment; filename="malicious.exe"""",
                date = fixedDate,
                timeZone = ist
            )
        )
    }

    @Test
    fun mapExportError_mapsNoProductsToFriendlyMessage() {
        val err = ProductCsvExportHelper.mapExportError(
            httpCode = 404,
            errorBody = """{"status":false,"message":"No products to export"}"""
        )
        assertFalse(err.isSessionExpired)
        assertTrue(err.message.contains("No products to export", ignoreCase = true))
    }

    @Test
    fun mapExportError_mapsRateLimitToFriendlyMessage() {
        val err = ProductCsvExportHelper.mapExportError(
            httpCode = 429,
            errorBody = """{"status":false,"message":"Too many attempts for export_products."}"""
        )
        assertFalse(err.isSessionExpired)
        assertTrue(err.message.contains("Too many exports", ignoreCase = true))
    }

    @Test
    fun mapExportError_mapsSessionExpiredForHttp401AndUnauthorizedException() {
        val errHttp = ProductCsvExportHelper.mapExportError(httpCode = 401)
        assertTrue(errHttp.isSessionExpired)
        assertTrue(errHttp.message.contains("session has expired", ignoreCase = true))

        val errEx = ProductCsvExportHelper.mapExportError(throwable = UnauthorizedException())
        assertTrue(errEx.isSessionExpired)
    }

    @Test
    fun mapExportError_mapsNetworkErrorsToFriendlyMessages() {
        val noHost = ProductCsvExportHelper.mapExportError(throwable = UnknownHostException("offline"))
        assertFalse(noHost.isSessionExpired)
        assertTrue(noHost.message.contains("No internet", ignoreCase = true))

        val connectErr = ProductCsvExportHelper.mapExportError(throwable = ConnectException("refused"))
        assertTrue(connectErr.message.contains("No internet", ignoreCase = true))

        val timeoutErr = ProductCsvExportHelper.mapExportError(throwable = SocketTimeoutException("timeout"))
        assertTrue(timeoutErr.message.contains("timed out", ignoreCase = true))

        val ioErr = ProductCsvExportHelper.mapExportError(throwable = IOException("stream reset"))
        assertFalse(ioErr.isSessionExpired)
        assertTrue(ioErr.message.isNotBlank())
    }

    @Test
    fun mapExportError_extractsServerJsonMessageAndHandlesMalformedBodySafely() {
        val serverErr = ProductCsvExportHelper.mapExportError(
            httpCode = 500,
            errorBody = """{"status":false,"message":"Database offline for maintenance."}"""
        )
        assertEquals("Database offline for maintenance.", serverErr.message)

        val htmlErr = ProductCsvExportHelper.mapExportError(
            httpCode = 502,
            errorBody = "<html><body>Bad Gateway</body></html>"
        )
        assertEquals("Could not export products (error 502). Please try again.", htmlErr.message)
    }
}
