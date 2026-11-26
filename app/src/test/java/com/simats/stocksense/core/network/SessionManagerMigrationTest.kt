package com.simats.stocksense.core.network

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.GeneralSecurityException

class SessionManagerMigrationTest {

    private class InMemorySharedPreferences(
        private val failReads: Boolean = false,
        private val failCommits: Boolean = false
    ) : SharedPreferences {
        private val map = linkedMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = LinkedHashMap(map)

        override fun getString(key: String?, defValue: String?): String? {
            if (failReads) throw GeneralSecurityException("Keystore read failed")
            return (map[key] as? String) ?: defValue
        }

        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues

        override fun getInt(key: String?, defValue: Int): Int {
            if (failReads) throw GeneralSecurityException("Keystore read failed")
            return (map[key] as? Int) ?: defValue
        }

        override fun getLong(key: String?, defValue: Long): Long {
            if (failReads) throw GeneralSecurityException("Keystore read failed")
            return (map[key] as? Long) ?: defValue
        }

        override fun getFloat(key: String?, defValue: Float): Float = (map[key] as? Float) ?: defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            if (failReads) throw GeneralSecurityException("Keystore read failed")
            return (map[key] as? Boolean) ?: defValue
        }

        override fun contains(key: String?): Boolean = map.containsKey(key)

        override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
            private val staged = linkedMapOf<String, Any?>()
            private val removals = linkedSetOf<String>()
            private var clearAll = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply {
                if (key != null) {
                    if (value == null) removals.add(key) else staged[key] = value
                }
            }

            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = apply {
                if (key != null) staged[key] = value
            }

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = apply {
                if (key != null) staged[key] = value
            }

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = apply {
                if (key != null) staged[key] = value
            }

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = apply {
                if (key != null) staged[key] = value
            }

            override fun remove(key: String?): SharedPreferences.Editor = apply {
                if (key != null) removals.add(key)
            }

            override fun clear(): SharedPreferences.Editor = apply {
                clearAll = true
            }

            override fun commit(): Boolean {
                if (failCommits) return false
                if (clearAll) map.clear()
                for (r in removals) map.remove(r)
                for ((k, v) in staged) map[k] = v
                return true
            }

            override fun apply() {
                commit()
            }
        }

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    @Test
    fun legacyPlainToken_isMigratedOnceAndPlainCopyIsDeleted() {
        val plain = InMemorySharedPreferences().apply {
            edit()
                .putBoolean(SessionManager.KEY_IS_LOGGED_IN, true)
                .putString(SessionManager.KEY_TOKEN, "legacy_token_64chars")
                .putString(SessionManager.KEY_EXPIRES_AT, "2099-12-31 23:59:59")
                .putString("shop_name", "Ananya Mart")
                .commit()
        }
        val secure = InMemorySharedPreferences()

        val storage = SessionTokenStorage(plain) { secure }

        assertEquals(TokenMigrationOutcome.MIGRATED, storage.migrationOutcome)
        assertTrue(storage.isLoggedIn {})
        assertEquals("legacy_token_64chars", storage.getToken {})
        assertEquals("2099-12-31 23:59:59", storage.getExpiresAt())

        // Plain copy removed, non-secret profile retained
        assertFalse(plain.contains(SessionManager.KEY_TOKEN))
        assertFalse(plain.contains(SessionManager.KEY_EXPIRES_AT))
        assertFalse(plain.contains(SessionManager.KEY_IS_LOGGED_IN))
        assertEquals("Ananya Mart", plain.getString("shop_name", null))

        // Second initialization does not re-migrate or wipe secure token
        val secondRun = SessionTokenStorage(plain) { secure }
        assertEquals(TokenMigrationOutcome.NOTHING_TO_MIGRATE, secondRun.migrationOutcome)
        assertEquals("legacy_token_64chars", secondRun.getToken {})
    }

    @Test
    fun alreadyMigratedSecureToken_isNotOverwrittenByStalePlainToken() {
        val plain = InMemorySharedPreferences().apply {
            edit()
                .putString(SessionManager.KEY_TOKEN, "old_plain_token")
                .commit()
        }
        val secure = InMemorySharedPreferences().apply {
            edit()
                .putBoolean(SessionManager.KEY_IS_LOGGED_IN, true)
                .putString(SessionManager.KEY_TOKEN, "newer_secure_token")
                .commit()
        }

        val storage = SessionTokenStorage(plain) { secure }

        assertEquals(TokenMigrationOutcome.ALREADY_MIGRATED_CLEANED_PLAIN, storage.migrationOutcome)
        assertEquals("newer_secure_token", storage.getToken {})
        assertFalse(plain.contains(SessionManager.KEY_TOKEN))
    }

    @Test
    fun encryptionInitFailure_fallsBackToLoggedOutWithoutCrashing() {
        val plain = InMemorySharedPreferences().apply {
            edit()
                .putBoolean(SessionManager.KEY_IS_LOGGED_IN, true)
                .putString(SessionManager.KEY_TOKEN, "legacy_token")
                .commit()
        }

        val storage = SessionTokenStorage(plain) {
            throw GeneralSecurityException("Hardware keystore broken")
        }

        assertEquals(TokenMigrationOutcome.ENCRYPTION_UNAVAILABLE, storage.migrationOutcome)
        assertFalse(storage.isLoggedIn {})
        assertNull(storage.getToken {})
        assertFalse(plain.contains(SessionManager.KEY_TOKEN))
    }

    @Test
    fun encryptionReadOrWriteFailure_neverCrashesAndAsksUserToRelogin() {
        val plain = InMemorySharedPreferences().apply {
            edit().putString(SessionManager.KEY_TOKEN, "legacy_token").commit()
        }
        val failingCommitSecure = InMemorySharedPreferences(failCommits = true)

        val failedMigrate = SessionTokenStorage(plain) { failingCommitSecure }
        assertEquals(TokenMigrationOutcome.MIGRATION_FAILED, failedMigrate.migrationOutcome)
        assertFalse(failedMigrate.isLoggedIn {})
        assertNull(failedMigrate.getToken {})
        assertFalse(plain.contains(SessionManager.KEY_TOKEN))

        val failingReadSecure = InMemorySharedPreferences(failReads = true)
        val failedRead = SessionTokenStorage(plain) { failingReadSecure }
        assertNull(failedRead.getToken {})
        assertFalse(failedRead.isLoggedIn {})
    }

    @Test
    fun saveToken_storesOnlyInEncryptedPrefsNeverInPlainPrefs() {
        val plain = InMemorySharedPreferences()
        val secure = InMemorySharedPreferences()
        val storage = SessionTokenStorage(plain) { secure }

        assertTrue(storage.saveToken("fresh_secure_token", "2099-01-01 00:00:00"))
        assertEquals("fresh_secure_token", storage.getToken {})
        assertEquals("fresh_secure_token", secure.getString(SessionManager.KEY_TOKEN, null))
        assertFalse(plain.contains(SessionManager.KEY_TOKEN))
        assertFalse(plain.contains(SessionManager.KEY_EXPIRES_AT))
        assertFalse(plain.contains(SessionManager.KEY_IS_LOGGED_IN))
    }
}
