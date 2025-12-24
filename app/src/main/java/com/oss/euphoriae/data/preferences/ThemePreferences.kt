package com.oss.euphoriae.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeColorOption(val displayName: String) {
    DYNAMIC("Dynamic"),
    PURPLE("Purple"),
    BLUE("Blue"),
    GREEN("Green"),
    ORANGE("Orange"),
    PINK("Pink"),
    RED("Red")
}

class ThemePreferences(private val context: Context) {
    
    companion object {
        private val THEME_COLOR_KEY = stringPreferencesKey("theme_color")
    }
    
    val themeColor: Flow<ThemeColorOption> = context.dataStore.data.map { preferences ->
        val colorName = preferences[THEME_COLOR_KEY] ?: ThemeColorOption.DYNAMIC.name
        try {
            ThemeColorOption.valueOf(colorName)
        } catch (e: IllegalArgumentException) {
            ThemeColorOption.DYNAMIC
        }
    }
    
    suspend fun setThemeColor(option: ThemeColorOption) {
        context.dataStore.edit { preferences ->
            preferences[THEME_COLOR_KEY] = option.name
        }
    }
}
