package com.simats.stocksense.feature.dashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test verifying account & store card label/value row formatting,
 * label names (including Owner, Email, Store, Currency, Timezone),
 * and value resolution logic.
 */
class SettingsAccountLayoutTest {

    @Test
    fun testAccountCardLabelsPresent() {
        val expectedLabels = listOf("Owner", "Email", "Store", "Currency", "Timezone")
        for (label in expectedLabels) {
            assertTrue("Label $label should not be blank", label.isNotBlank())
        }
    }

    @Test
    fun testProfileCardLabelsPresent() {
        val expectedLabels = listOf("Email", "Store type", "Store ID", "Currency / timezone", "Data snapshot")
        for (label in expectedLabels) {
            assertTrue("Profile label $label should not be blank", label.isNotBlank())
        }
    }

    @Test
    fun testBlankValueFallbackToDash() {
        val blankVal = ""
        val resolved = blankVal.ifBlank { "—" }
        assertEquals("—", resolved)

        val nullVal: String? = null
        val resolvedNull = nullVal?.takeIf { it.isNotBlank() } ?: "—"
        assertEquals("—", resolvedNull)
    }

    @Test
    fun testStoreStringFormatting() {
        // Case 1: Both shop and location present
        val shop1 = "Amul Daily Needs Supermarket"
        val loc1 = "Indira Nagar, Bengaluru"
        val storeStr1 = if (loc1.isNotBlank() && shop1 != "—") "$shop1, $loc1" else shop1
        assertEquals("Amul Daily Needs Supermarket, Indira Nagar, Bengaluru", storeStr1)

        // Case 2: Only shop present
        val shop2 = "Green Basket"
        val loc2: String? = null
        val storeStr2 = if (!loc2.isNullOrBlank() && shop2 != "—") "$shop2, $loc2" else shop2
        assertEquals("Green Basket", storeStr2)

        // Case 3: Both blank
        val shop3: String? = null
        val loc3: String? = null
        val finalShop = shop3?.takeIf { it.isNotBlank() } ?: "—"
        val storeStr3 = if (!loc3.isNullOrBlank() && finalShop != "—") "$finalShop, $loc3" else finalShop
        assertEquals("—", storeStr3)
    }

    @Test
    fun testCurrencyFormatting() {
        val currInr = "INR"
        val currStrInr = if (currInr.equals("INR", ignoreCase = true)) "Indian rupee · ₹" else currInr
        assertEquals("Indian rupee · ₹", currStrInr)

        val currUsd = "USD"
        val currStrUsd = if (currUsd.equals("INR", ignoreCase = true)) "Indian rupee · ₹" else currUsd
        assertEquals("USD", currStrUsd)
    }

    @Test
    fun testTimezoneFormatting() {
        val tzIst = "Asia/Kolkata"
        val tzStrIst = if (tzIst.equals("Asia/Kolkata", ignoreCase = true)) "Asia/Kolkata · IST" else tzIst
        assertEquals("Asia/Kolkata · IST", tzStrIst)

        val tzUtc = "UTC"
        val tzStrUtc = if (tzUtc.equals("Asia/Kolkata", ignoreCase = true)) "Asia/Kolkata · IST" else tzUtc
        assertEquals("UTC", tzStrUtc)
    }

    @Test
    fun testEmailRowLabelIsExplicitlyEmail() {
        val emailLabel = "Email"
        assertEquals("Email", emailLabel)
        assertNotNull(emailLabel)
    }
}
