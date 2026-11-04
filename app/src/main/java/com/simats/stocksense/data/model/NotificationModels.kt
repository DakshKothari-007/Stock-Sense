package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName

/**
 * Notifications models strictly adhering to API_DOCUMENT.md Module 10.
 * All fields are nullable with safe defaults to prevent JSON parse crashes.
 */
data class NotificationsResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: NotificationsData? = null
)

data class NotificationsData(
    @SerializedName("unread_count") val unreadCount: Int? = 0,
    @SerializedName("total_count") val totalCount: Int? = 0,
    @SerializedName("page") val page: Int? = 1,
    @SerializedName("limit") val limit: Int? = 50,
    @SerializedName("items") val items: List<NotificationItem>? = emptyList()
)

data class NotificationItem(
    @SerializedName("key") val key: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("numbers_used") val numbersUsed: Map<String, Any?>? = null,
    @SerializedName("created_for_date") val createdForDate: String? = null,
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("is_read") val isRead: Boolean? = false,
    @SerializedName("read_at") val readAt: String? = null
)

data class MarkReadRequest(
    @SerializedName("key") val key: String
)

data class MarkReadResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: MarkReadData? = null
)

data class MarkReadData(
    @SerializedName("key") val key: String? = null,
    @SerializedName("is_read") val isRead: Boolean? = false
)

data class MarkAllReadResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: MarkAllReadData? = null
)

data class MarkAllReadData(
    @SerializedName("marked_count") val markedCount: Int? = 0
)

data class DismissNotificationRequest(
    @SerializedName("key") val key: String
)

data class DismissNotificationResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: DismissNotificationData? = null
)

data class DismissNotificationData(
    @SerializedName("key") val key: String? = null,
    @SerializedName("dismissed") val dismissed: Boolean? = false
)

data class RegisterDeviceRequest(
    @SerializedName("fcm_token") val fcmToken: String,
    @SerializedName("platform") val platform: String = "android"
)

data class RegisterDeviceResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: Map<String, Any?>? = null
)

data class UnregisterDeviceRequest(
    @SerializedName("fcm_token") val fcmToken: String
)

data class UnregisterDeviceResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: Any? = null
)

