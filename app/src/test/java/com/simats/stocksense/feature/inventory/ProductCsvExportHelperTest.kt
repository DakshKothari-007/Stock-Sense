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

    @Test
    fun sanitizeExportFileName_emptyAndSpacesUseDefaultName() {
        val ist = TimeZone.getTimeZone("Asia/Kolkata")
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.NOVEMBER, 25, 18, 45, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fixedDate = cal.time

        val emptyRes = ProductCsvExportHelper.sanitizeExportFileName("", fixedDate, ist)
        assertEquals("stocksense-products-20261126", emptyRes.baseName)
        assertEquals("stocksense-products-20261126.csv", emptyRes.fileName)
        assertTrue(emptyRes.usedDefault)
        assertFalse(emptyRes.charactersReplaced)

        val spacesRes = ProductCsvExportHelper.sanitizeExportFileName("     ", fixedDate, ist)
        assertEquals("stocksense-products-20261126", spacesRes.baseName)
        assertEquals("stocksense-products-20261126.csv", spacesRes.fileName)
        assertTrue(spacesRes.usedDefault)
        assertFalse(spacesRes.charactersReplaced)
    }

    @Test
    fun sanitizeExportFileName_replacesDisallowedCharsAndFlagsReplacement() {
        val res = ProductCsvExportHelper.sanitizeExportFileName("sales/2026:march*report?")
        assertEquals("sales_2026_march_report_", res.baseName)
        assertEquals("sales_2026_march_report_.csv", res.fileName)
        assertTrue(res.charactersReplaced)
        assertFalse(res.usedDefault)
    }

    @Test
    fun sanitizeExportFileName_stripsTrailingCsvWithoutDoublingOrReplacementHint() {
        val resLower = ProductCsvExportHelper.sanitizeExportFileName("my_shop_items.csv")
        assertEquals("my_shop_items", resLower.baseName)
        assertEquals("my_shop_items.csv", resLower.fileName)
        assertFalse(resLower.charactersReplaced)

        val resUpper = ProductCsvExportHelper.sanitizeExportFileName("  My Stock.CSV ")
        assertEquals("My Stock", resUpper.baseName)
        assertEquals("My Stock.csv", resUpper.fileName)
        assertFalse(resUpper.charactersReplaced)
    }

    @Test
    fun sanitizeExportFileName_truncatesVeryLongNameTo60Chars() {
        val longInput = "a".repeat(85)
        val res = ProductCsvExportHelper.sanitizeExportFileName(longInput)
        assertEquals(ProductCsvExportHelper.MAX_EXPORT_NAME_LENGTH, res.baseName.length)
        assertEquals("a".repeat(60) + ".csv", res.fileName)
        assertFalse(res.usedDefault)
    }

    @Test
    fun sanitizeExportFileName_preservesTamilAndHindiUnicodeNames() {
        val tamilRes = ProductCsvExportHelper.sanitizeExportFileName("அரிசி பருப்பு-2026")
        assertEquals("அரிசி பருப்பு-2026", tamilRes.baseName)
        assertEquals("அரிசி பருப்பு-2026.csv", tamilRes.fileName)
        assertFalse(tamilRes.charactersReplaced)

        val hindiRes = ProductCsvExportHelper.sanitizeExportFileName("राशन सूची_नवंबर 2026")
        assertEquals("राशन सूची_नवंबर 2026", hindiRes.baseName)
        assertEquals("राशन सूची_नवंबर 2026.csv", hindiRes.fileName)
        assertFalse(hindiRes.charactersReplaced)
    }

    @Test
    fun sanitizeExportFileName_blocksDotAndDotDotWithDefaultFallback() {
        val ist = TimeZone.getTimeZone("Asia/Kolkata")
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.NOVEMBER, 25, 18, 45, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fixedDate = cal.time

        val dotDotRes = ProductCsvExportHelper.sanitizeExportFileName("..", fixedDate, ist)
        assertEquals("stocksense-products-20261126", dotDotRes.baseName)
        assertEquals("stocksense-products-20261126.csv", dotDotRes.fileName)
        assertTrue(dotDotRes.usedDefault)

        val singleDotRes = ProductCsvExportHelper.sanitizeExportFileName(".", fixedDate, ist)
        assertEquals("stocksense-products-20261126.csv", singleDotRes.fileName)
        assertTrue(singleDotRes.usedDefault)
    }

    @Test
    fun sanitizeExportFileName_keepsNormalNameUntouched() {
        val res = ProductCsvExportHelper.sanitizeExportFileName("  Kirana Store Inventory-2026_11  ")
        assertEquals("Kirana Store Inventory-2026_11", res.baseName)
        assertEquals("Kirana Store Inventory-2026_11.csv", res.fileName)
        assertFalse(res.charactersReplaced)
        assertFalse(res.usedDefault)
    }
}
