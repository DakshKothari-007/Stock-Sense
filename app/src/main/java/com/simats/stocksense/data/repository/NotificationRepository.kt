package com.simats.stocksense.data.repository

import com.google.gson.Gson
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.NotificationApiService
import com.simats.stocksense.core.network.SessionManager
import com.simats.stocksense.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for notifications operations adhering to API_DOCUMENT.md Module 10.
 */
class NotificationRepository(
    private val apiService: NotificationApiService = NetworkClient.createService(),
    private val gson: Gson = NetworkClient.gson
) {

    suspend fun fetchNotifications(
        type: String? = null,
        unreadOnly: Boolean? = null,
        limit: Int? = null,
        page: Int? = null
    ): Result<NotificationsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNotifications(
                type = type?.takeIf { it.isNotBlank() },
                unreadOnly = unreadOnly,
                limit = limit,
                page = page
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message?.ifBlank { "Failed to retrieve notifications." } ?: "Failed to retrieve notifications."))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUnreadCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUnreadCount()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data.unreadCount ?: 0)
                } else {
                    Result.success(0)
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markRead(key: String): Result<MarkReadData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markRead(MarkReadRequest(key = key))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message?.ifBlank { "Failed to mark notification as read." } ?: "Failed to mark notification as read."))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllRead(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markAllRead()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data.markedCount ?: 0)
                } else {
                    Result.failure(Exception(body?.message?.ifBlank { "Failed to mark all notifications as read." } ?: "Failed to mark all notifications as read."))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dismiss(key: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.dismissNotification(DismissNotificationRequest(key = key))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true) {
                    Result.success(body.data?.dismissed == true)
                } else {
                    Result.failure(Exception(body?.message?.ifBlank { "Failed to dismiss notification." } ?: "Failed to dismiss notification."))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerDevice(token: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.registerDevice(RegisterDeviceRequest(fcmToken = token))
            if (response.isSuccessful && response.body()?.status == true) {
                Result.success(true)
            } else {
                val body = response.body()
                Result.failure(Exception(body?.message?.ifBlank { "Failed to register device." } ?: "Failed to register device."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unregisterDevice(token: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.unregisterDevice(UnregisterDeviceRequest(fcmToken = token))
            if (response.isSuccessful && response.body()?.status == true) {
                Result.success(true)
            } else {
                val body = response.body()
                Result.failure(Exception(body?.message?.ifBlank { "Failed to unregister device." } ?: "Failed to unregister device."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> handleHttpError(code: Int, errorBody: String?): Result<T> {
        if (code == 401) {
            NetworkClient.getSessionManager()?.clearSession()
            return Result.failure(Exception("Session expired. Please log in again."))
        }
        val msg = extractErrorMessage(errorBody, code)
        return Result.failure(Exception(msg))
    }

    private fun extractErrorMessage(errorBody: String?, fallbackCode: Int): String {
        if (errorBody.isNullOrBlank()) return "Server returned error code $fallbackCode"
        return try {
            val map = gson.fromJson(errorBody, Map::class.java)
            map["message"]?.toString() ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}
