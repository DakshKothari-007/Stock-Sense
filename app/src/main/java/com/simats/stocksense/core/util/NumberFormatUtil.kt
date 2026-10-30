package com.simats.stocksense.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for Indian numbering system formatting and date formatting.
 * Formats currencies as ₹1,48,620, counts as 1,48,620, etc.
 */
object NumberFormatUtil {

    private val indianLocale = Locale.forLanguageTag("en-IN")

    /**
     * Formats an amount into Indian Rupees format (e.g. ₹1,48,620 or ₹18,450.50).
     */
    fun formatIndianRupees(amount: Double?): String {
        if (amount == null) return "₹0"
        return try {
            val format = NumberFormat.getCurrencyInstance(indianLocale).apply {
                maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
                minimumFractionDigits = 0
                val dfs = (this as? DecimalFormat)?.decimalFormatSymbols ?: DecimalFormatSymbols(indianLocale)
                dfs.currencySymbol = "₹"
                (this as? DecimalFormat)?.decimalFormatSymbols = dfs
            }
            format.format(amount)
                .replace("Rs.", "₹")
                .replace("INR", "₹")
                .trim()
        } catch (e: Exception) {
            "₹${String.format(Locale.US, "%.0f", amount)}"
        }
    }

    /**
     * Formats an integer count with Indian digit grouping (e.g. 1,48,620).
     */
    fun formatIndianNumber(count: Long?): String {
        if (count == null) return "0"
        return try {
            val format = NumberFormat.getInstance(indianLocale).apply {
                maximumFractionDigits = 0
            }
            format.format(count)
        } catch (e: Exception) {
            count.toString()
        }
    }

    fun formatIndianNumber(count: Int?): String = formatIndianNumber(count?.toLong())

    /**
     * Formats current date for dashboard header (e.g. "Thu, 30 Oct 2026").
     */
    fun formatCurrentHeaderDate(): String {
        return try {
            val sdf = SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH)
            sdf.format(Date())
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Formats current time for last updated subtitle (e.g. "30 Oct, 1:45 pm").
     */
    fun formatLastUpdatedTime(date: Date = Date()): String {
        return try {
            val sdf = SimpleDateFormat("d MMM, h:mm a", Locale.ENGLISH)
            sdf.format(date).lowercase(Locale.ENGLISH)
        } catch (e: Exception) {
            ""
        }
    }
}
