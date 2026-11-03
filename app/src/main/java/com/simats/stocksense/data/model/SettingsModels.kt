package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName

/**
 * Top-level response for Settings API endpoints.
 */
data class SettingsResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SettingsData? = null
)

/**
 * Settings data payload returned by settings/get.php, settings/update.php, and settings/reset_defaults.php.
 */
data class SettingsData(
    @SerializedName("account") val account: SettingsAccountData? = null,
    @SerializedName("appearance") val appearance: SettingsAppearanceData? = null,
    @SerializedName("notifications") val notifications: SettingsNotificationsData? = null,
    @SerializedName("inventory") val inventory: SettingsInventoryData? = null,
    @SerializedName("is_customized") val isCustomized: Boolean? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class SettingsAccountData(
    @SerializedName("owner_name") val ownerName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("shop_name") val shopName: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("timezone") val timezone: String? = null
)

data class SettingsAppearanceData(
    @SerializedName("theme_mode") val themeMode: String? = "SYSTEM"
)

data class SettingsNotificationsData(
    @SerializedName("low_stock_alerts") val lowStockAlerts: Boolean? = true,
    @SerializedName("ai_recommendations") val aiRecommendations: Boolean? = true,
    @SerializedName("daily_sales_summary") val dailySalesSummary: Boolean? = true,
    @SerializedName("weekly_email_digest") val weeklyEmailDigest: Boolean? = false
)

data class SettingsInventoryData(
    @SerializedName("import_mode") val importMode: String? = "ADD",
    @SerializedName("lead_time_days") val leadTimeDays: Int? = 2,
    @SerializedName("safety_days") val safetyDays: Int? = 3,
    @SerializedName("cover_days") val coverDays: Int? = 7,
    @SerializedName("min_history_days") val minHistoryDays: Int? = 30,
    @SerializedName("slow_moving_threshold_days") val slowMovingThresholdDays: Int? = 60
)

/**
 * Request payload for settings/change_password.php.
 */
data class ChangePasswordSettingsRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String? = null
)

