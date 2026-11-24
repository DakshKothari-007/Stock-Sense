package com.simats.stocksense.feature.analytics.util

import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.feature.analytics.model.BillReceiptData
import com.simats.stocksense.feature.analytics.model.BillReceiptItem
import com.simats.stocksense.feature.analytics.model.PhoneNumberValidationResult
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ReceiptFormatter {

    /**
     * Cleans and validates optional phone number:
     * - Strips spaces and dashes.
     * - If empty / blank -> Empty.
     * - If 10 digits -> adds '91' country code.
     * - If 11 digits starting with '0' -> strips '0' and adds '91'.
     * - If 12 digits starting with '91' -> Valid.
     * - Otherwise -> Invalid with clear message.
     */
    fun cleanPhoneNumber(raw: String?): PhoneNumberValidationResult {
        if (raw == null || raw.trim().isEmpty()) {
            return PhoneNumberValidationResult.Empty
        }

        var cleaned = raw.replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .trim()

        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1)
        }

        if (cleaned.isEmpty()) {
            return PhoneNumberValidationResult.Empty
        }

        if (!cleaned.all { it.isDigit() }) {
            return PhoneNumberValidationResult.Invalid("Phone number must contain digits only.")
        }

        if (cleaned.length == 10) {
            return PhoneNumberValidationResult.Valid("91$cleaned")
        }

        if (cleaned.length == 11 && cleaned.startsWith("0")) {
            return PhoneNumberValidationResult.Valid("91${cleaned.substring(1)}")
        }

        if (cleaned.length == 12 && cleaned.startsWith("91")) {
            return PhoneNumberValidationResult.Valid(cleaned)
        }

        return PhoneNumberValidationResult.Invalid("Please enter a valid 10-digit mobile number.")
    }

    /**
     * Formats currency amount in Indian Rupees (e.g. ₹1,480).
     */
    fun formatAmount(amount: Double): String {
        return NumberFormatUtil.formatIndianRupees(amount)
    }

    /**
     * Formats a single bill line item string for summary / display.
     */
    fun formatLineItem(item: BillReceiptItem): String {
        val priceStr = formatAmount(item.unitPrice)
        val totalStr = formatAmount(item.lineTotal)
        return "${item.name} (${item.quantity} × $priceStr) = $totalStr"
    }

    /**
     * Formats bill total summary string.
     */
    fun formatBillTotal(totalAmount: Double, totalQuantity: Int, totalItems: Int): String {
        val amountStr = formatAmount(totalAmount)
        return "Total: $amountStr ($totalItems items · $totalQuantity units)"
    }

    /**
     * Formats plain text receipt for text sharing and wa.me fallback.
     */
    fun formatPlainTextBill(bill: BillReceiptData): String {
        val sb = StringBuilder()
        sb.appendLine("🧾 *${bill.shopName}*")
        sb.appendLine("Bill No: ${bill.billNo}")
        sb.appendLine("Date: ${bill.dateFormatted}")
        sb.appendLine("────────────────────────────")
        bill.items.forEach { item ->
            sb.appendLine(item.name)
            sb.appendLine("   ${item.quantity} × ${formatAmount(item.unitPrice)} = ${formatAmount(item.lineTotal)}")
        }
        sb.appendLine("────────────────────────────")
        sb.appendLine("*TOTAL: ${formatAmount(bill.totalAmount)}*")
        sb.appendLine("Items: ${bill.items.size} | Units: ${bill.totalQuantity}")
        sb.appendLine("────────────────────────────")
        sb.appendLine(bill.thankYouMessage)
        return sb.toString().trim()
    }

    /**
     * Builds wa.me link with URL-encoded bill text.
     */
    fun buildWaMeUrl(phoneNumber: String?, bill: BillReceiptData): String {
        val text = formatPlainTextBill(bill)
        val encodedText = try {
            URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            text.replace(" ", "%20")
        }

        return if (!phoneNumber.isNullOrBlank()) {
            "https://wa.me/$phoneNumber?text=$encodedText"
        } else {
            "https://wa.me/?text=$encodedText"
        }
    }
}

