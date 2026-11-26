package com.simats.stocksense.core.fcm

import android.content.Intent
import com.simats.stocksense.PushNotificationPayload

/**
 * Validates incoming notification intent extras against a strict push type whitelist
 * and positive integer product_id rules before allowing deep-link navigation.
 */
object NotificationIntentValidator {

    val ALLOWED_PUSH_TYPES: Set<String> = setOf(
        "low_stock",
        "reorder_suggestion",
        "ai_recommendation",
        "daily_summary"
    )

    fun validate(
        rawType: String?,
        rawProductIdStr: String?,
        rawProductIdInt: Int? = null
    ): PushNotificationPayload? {
        val pushType = rawType?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (pushType !in ALLOWED_PUSH_TYPES) {
            return null
        }

        val parsedProductId: Int? = when {
            rawProductIdStr != null -> {
                if (rawProductIdStr.isEmpty()) {
                    null
                } else {
                    val trimmedId = rawProductIdStr.trim()
                    val parsed = trimmedId.toIntOrNull()
                    if (trimmedId.isEmpty() || parsed == null || parsed <= 0) {
                        return null
                    }
                    parsed
                }
            }
            rawProductIdInt != null -> {
                if (rawProductIdInt <= 0) {
                    return null
                }
                rawProductIdInt
            }
            else -> null
        }

        if (pushType == "low_stock" && parsedProductId == null) {
            return null
        }

        return PushNotificationPayload(
            type = pushType,
            productId = parsedProductId
        )
    }

    fun fromIntent(intent: Intent?): PushNotificationPayload? {
        if (intent == null) return null

        val rawType = intent.getStringExtra(StockSenseMessagingService.EXTRA_PUSH_TYPE)
            ?.trim()?.takeIf { it.isNotEmpty() }
            ?: intent.getStringExtra("type")?.trim()?.takeIf { it.isNotEmpty() }

        val rawProductIdStr = when {
            intent.hasExtra(StockSenseMessagingService.EXTRA_PRODUCT_ID) -> {
                intent.getStringExtra(StockSenseMessagingService.EXTRA_PRODUCT_ID)
                    ?.takeIf { it.isNotEmpty() }
                    ?: intent.getStringExtra("product_id")
                    ?: intent.getStringExtra(StockSenseMessagingService.EXTRA_PRODUCT_ID)
            }
            intent.hasExtra("product_id") -> {
                intent.getStringExtra("product_id")
            }
            else -> null
        }

        val rawProductIdInt: Int? = if (rawProductIdStr == null) {
            when {
                intent.hasExtra(StockSenseMessagingService.EXTRA_PRODUCT_ID) -> {
                    val v = intent.getIntExtra(StockSenseMessagingService.EXTRA_PRODUCT_ID, Int.MIN_VALUE)
                    if (v != Int.MIN_VALUE) v else -1
                }
                intent.hasExtra("product_id") -> {
                    val v = intent.getIntExtra("product_id", Int.MIN_VALUE)
                    if (v != Int.MIN_VALUE) v else -1
                }
                else -> null
            }
        } else {
            null
        }

        return validate(
            rawType = rawType,
            rawProductIdStr = rawProductIdStr,
            rawProductIdInt = rawProductIdInt
        )
    }
}
