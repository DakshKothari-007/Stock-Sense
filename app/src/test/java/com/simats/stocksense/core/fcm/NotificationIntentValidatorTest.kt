package com.simats.stocksense.core.fcm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationIntentValidatorTest {

    @Test
    fun validate_acceptsLowStockWithPositiveIntegerStringOrInt() {
        val fromStr = NotificationIntentValidator.validate("low_stock", "42")
        assertEquals("low_stock", fromStr?.type)
        assertEquals(42, fromStr?.productId)

        val fromInt = NotificationIntentValidator.validate("low_stock", null, rawProductIdInt = 9)
        assertEquals("low_stock", fromInt?.type)
        assertEquals(9, fromInt?.productId)
    }

    @Test
    fun validate_acceptsWhitelistedTypesWithoutProductIdWhereOptional() {
        val daily = NotificationIntentValidator.validate("daily_summary", "")
        assertEquals("daily_summary", daily?.type)
        assertNull(daily?.productId)

        val aiRec = NotificationIntentValidator.validate("ai_recommendation", null)
        assertEquals("ai_recommendation", aiRec?.type)
        assertNull(aiRec?.productId)

        val reorderEmpty = NotificationIntentValidator.validate("reorder_suggestion", "")
        assertEquals("reorder_suggestion", reorderEmpty?.type)
        assertNull(reorderEmpty?.productId)

        val reorderWithId = NotificationIntentValidator.validate("reorder_suggestion", "15")
        assertEquals("reorder_suggestion", reorderWithId?.type)
        assertEquals(15, reorderWithId?.productId)
    }

    @Test
    fun validate_rejectsUnknownOrBlankPushType() {
        assertNull(NotificationIntentValidator.validate(null, "1"))
        assertNull(NotificationIntentValidator.validate("", "1"))
        assertNull(NotificationIntentValidator.validate("   ", "1"))
        assertNull(NotificationIntentValidator.validate("unknown_type", "1"))
        assertNull(NotificationIntentValidator.validate("delete_account", "1"))
        assertNull(NotificationIntentValidator.validate("LOW_STOCK", "1"))
    }

    @Test
    fun validate_rejectsInvalidOrNonPositiveProductIds() {
        assertNull(NotificationIntentValidator.validate("low_stock", null))
        assertNull(NotificationIntentValidator.validate("low_stock", ""))
        assertNull(NotificationIntentValidator.validate("low_stock", "0"))
        assertNull(NotificationIntentValidator.validate("low_stock", "-5"))
        assertNull(NotificationIntentValidator.validate("low_stock", "abc"))
        assertNull(NotificationIntentValidator.validate("low_stock", "1; DROP TABLE"))
        assertNull(NotificationIntentValidator.validate("low_stock", "   "))
        assertNull(NotificationIntentValidator.validate("low_stock", null, rawProductIdInt = 0))
        assertNull(NotificationIntentValidator.validate("low_stock", null, rawProductIdInt = -3))

        assertNull(NotificationIntentValidator.validate("daily_summary", "-1"))
        assertNull(NotificationIntentValidator.validate("daily_summary", "bad"))
        assertNull(NotificationIntentValidator.validate("reorder_suggestion", "0"))
    }
}
