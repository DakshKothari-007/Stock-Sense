package com.simats.stocksense.feature.analytics.model

import androidx.annotation.Keep

@Keep
data class BillReceiptData(
    val shopName: String,
    val billNo: String,
    val dateFormatted: String,
    val items: List<BillReceiptItem>,
    val totalAmount: Double,
    val totalQuantity: Int,
    val totalItems: Int = items.size,
    val thankYouMessage: String = "Thank you for shopping with us! Please visit again."
)

@Keep
data class BillReceiptItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)

sealed class PhoneNumberValidationResult {
    object Empty : PhoneNumberValidationResult()
    data class Valid(val formattedNumber: String) : PhoneNumberValidationResult()
    data class Invalid(val errorMessage: String) : PhoneNumberValidationResult()
}

