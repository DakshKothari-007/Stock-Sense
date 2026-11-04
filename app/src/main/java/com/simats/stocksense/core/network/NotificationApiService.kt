package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit API service for StockSense Notifications (Module 10).
 */
interface NotificationApiService {

    @GET("notifications/list.php")
    suspend fun getNotifications(
        @Query("type") type: String? = null,
        @Query("unread_only") unreadOnly: Boolean? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null
    ): Response<NotificationsResponse>

    @GET("notifications/unread_count.php")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @POST("notifications/mark_read.php")
    suspend fun markRead(
        @Body request: MarkReadRequest
    ): Response<MarkReadResponse>

    @POST("notifications/mark_all_read.php")
    suspend fun markAllRead(
        @Body emptyBody: Map<String, String> = emptyMap()
    ): Response<MarkAllReadResponse>

    @POST("notifications/dismiss.php")
    suspend fun dismissNotification(
        @Body request: DismissNotificationRequest
    ): Response<DismissNotificationResponse>

    @POST("notifications/register_device.php")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest
    ): Response<RegisterDeviceResponse>

    @POST("notifications/unregister_device.php")
    suspend fun unregisterDevice(
        @Body request: UnregisterDeviceRequest
    ): Response<UnregisterDeviceResponse>
}

