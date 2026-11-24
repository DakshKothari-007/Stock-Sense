package com.simats.stocksense.feature.analytics

import com.simats.stocksense.feature.analytics.model.BillReceiptData
import com.simats.stocksense.feature.analytics.model.BillReceiptItem
import com.simats.stocksense.feature.analytics.model.PhoneNumberValidationResult
import com.simats.stocksense.feature.analytics.util.ReceiptFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BillReceiptTest {

    // ==========================================
    // 1. Phone Number Cleaning Tests
    // ==========================================

    @Test
    fun cleanPhoneNumber_standard10Digits_prepends91() {
        val result = ReceiptFormatter.cleanPhoneNumber("9876543210")
        assertTrue(result is PhoneNumberValidationResult.Valid)
        assertEquals("919876543210", (result as PhoneNumberValidationResult.Valid).formattedNumber)
    }

    @Test
    fun cleanPhoneNumber_withSpacesAndDashes_cleansAndPrepends91() {
        val inputs = listOf(
            "98765 43210",
            "98765-43210",
            " 98765 - 43210 ",
            "(98765) 43210"
        )
        for (input in inputs) {
            val result = ReceiptFormatter.cleanPhoneNumber(input)
            assertTrue("Failed for input: $input", result is PhoneNumberValidationResult.Valid)
            assertEquals("919876543210", (result as PhoneNumberValidationResult.Valid).formattedNumber)
        }
    }

    @Test
    fun cleanPhoneNumber_withLeadingPlus91_returnsValid() {
        val result = ReceiptFormatter.cleanPhoneNumber("+91 98765-43210")
        assertTrue(result is PhoneNumberValidationResult.Valid)
        assertEquals("919876543210", (result as PhoneNumberValidationResult.Valid).formattedNumber)
    }

    @Test
    fun cleanPhoneNumber_already12DigitsWith91_returnsValid() {
        val result = ReceiptFormatter.cleanPhoneNumber("919876543210")
        assertTrue(result is PhoneNumberValidationResult.Valid)
        assertEquals("919876543210", (result as PhoneNumberValidationResult.Valid).formattedNumber)
    }

    @Test
    fun cleanPhoneNumber_11DigitsStartingWithZero_stripsZeroAndPrepends91() {
        val result = ReceiptFormatter.cleanPhoneNumber("09876543210")
        assertTrue(result is PhoneNumberValidationResult.Valid)
        assertEquals("919876543210", (result as PhoneNumberValidationResult.Valid).formattedNumber)
    }

    @Test
    fun cleanPhoneNumber_invalidLengthsAndNonDigits_returnsInvalid() {
        val invalidInputs = listOf(
            "12345",              // Too short
            "123456789",          // 9 digits
            "987654321012345",    // Too long
            "abcdefghij",         // Letters
            "98765abcd0"          // Mixed alphanumeric
        )
        for (input in invalidInputs) {
            val result = ReceiptFormatter.cleanPhoneNumber(input)
            assertTrue("Expected invalid for: $input", result is PhoneNumberValidationResult.Invalid)
            val message = (result as PhoneNumberValidationResult.Invalid).errorMessage
            assertTrue(message.isNotBlank())
        }
    }

    // ==========================================
    // 2. Empty-Number Case Tests
    // ==========================================

    @Test
    fun cleanPhoneNumber_nullOrEmptyOrWhitespace_returnsEmpty() {
        val emptyInputs = listOf(
            null,
            "",
            "   ",
            "\t\n",
            " -  - "
        )
        for (input in emptyInputs) {
            val result = ReceiptFormatter.cleanPhoneNumber(input)
            assertTrue("Expected Empty for: '$input'", result is PhoneNumberValidationResult.Empty)
        }
    }

    @Test
    fun buildWaMeUrl_withEmptyNumber_omitsPhoneNumber() {
        val bill = createSampleBill()
        val urlNull = ReceiptFormatter.buildWaMeUrl(null, bill)
        assertTrue(urlNull.startsWith("https://wa.me/?text="))

        val urlBlank = ReceiptFormatter.buildWaMeUrl("   ", bill)
        assertTrue(urlBlank.startsWith("https://wa.me/?text="))
    }

    @Test
    fun buildWaMeUrl_withValidNumber_includesPhoneNumber() {
        val bill = createSampleBill()
        val url = ReceiptFormatter.buildWaMeUrl("919876543210", bill)
        assertTrue(url.startsWith("https://wa.me/919876543210?text="))
    }

    // ==========================================
    // 3. Bill Total & Line Item Formatting Tests
    // ==========================================

    @Test
    fun formatLineItem_formatsItemNameQuantityPriceAndLineTotal() {
        val item = BillReceiptItem(
            name = "Aashirvaad Atta 5kg",
            quantity = 2,
            unitPrice = 280.0,
            lineTotal = 560.0
        )
        val formatted = ReceiptFormatter.formatLineItem(item)
        assertTrue(formatted.contains("Aashirvaad Atta 5kg"))
        assertTrue(formatted.contains("2"))
        assertTrue(formatted.contains("280"))
        assertTrue(formatted.contains("560"))
    }

    @Test
    fun formatBillTotal_formatsAmountItemsAndUnits() {
        val totalSummary = ReceiptFormatter.formatBillTotal(
            totalAmount = 1480.0,
            totalQuantity = 5,
            totalItems = 3
        )
        assertTrue(totalSummary.contains("1,480"))
        assertTrue(totalSummary.contains("3 items"))
        assertTrue(totalSummary.contains("5 units"))
    }

    @Test
    fun formatPlainTextBill_containsAllRequiredBillElements() {
        val bill = createSampleBill()
        val plainText = ReceiptFormatter.formatPlainTextBill(bill)

        // Shop name & bill identifiers
        assertTrue(plainText.contains("Krishna Supermarket"))
        assertTrue(plainText.contains("APP-20261124-001"))
        assertTrue(plainText.contains("24 Nov 2026, 08:30 AM"))

        // Item details
        assertTrue(plainText.contains("Amul Butter 500g"))
        assertTrue(plainText.contains("Tata Salt 1kg"))

        // Totals & Thank you message
        assertTrue(plainText.contains("TOTAL:"))
        assertTrue(plainText.contains("350"))
        assertTrue(plainText.contains("Thank you for shopping with us!"))
    }

    private fun createSampleBill(): BillReceiptData {
        return BillReceiptData(
            shopName = "Krishna Supermarket",
            billNo = "APP-20261124-001",
            dateFormatted = "24 Nov 2026, 08:30 AM",
            items = listOf(
                BillReceiptItem("Amul Butter 500g", quantity = 1, unitPrice = 275.0, lineTotal = 275.0),
                BillReceiptItem("Tata Salt 1kg", quantity = 3, unitPrice = 25.0, lineTotal = 75.0)
            ),
            totalAmount = 350.0,
            totalQuantity = 4,
            totalItems = 2,
            thankYouMessage = "Thank you for shopping with us! Please visit again."
        )
    }
}

