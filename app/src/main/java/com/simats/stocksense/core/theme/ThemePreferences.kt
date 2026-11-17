package com.simats.stocksense.core.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "stocksense_theme_prefs")

object ThemePreferences {
    private val THEME_KEY = stringPreferencesKey("theme_mode")

    fun getThemeMode(context: Context): Flow<ThemeMode> {
        return context.themeDataStore.data.map { preferences ->
            when (preferences[THEME_KEY]) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
    }

    suspend fun saveThemeMode(context: Context, mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }
}
