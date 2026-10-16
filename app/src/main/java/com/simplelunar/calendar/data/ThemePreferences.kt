package com.simplelunar.calendar.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore by preferencesDataStore(name = "theme_settings")

enum class ThemeChoice(val label: String, val hue: Float) {
    RED("喜氣紅", 2f),
    PINK("珊瑚粉", 354f),
    PEACH("蜜桃", 17f),
    BERRY("莓紅", 339f),
    ORANGE("柑橘橙", 26f),
    GOLD("暖陽金", 41f),
    CUSTOM("自訂色彩", 350f),
}

enum class DisplayMode(val label: String) {
    SYSTEM("系統"),
    LIGHT("淺色"),
    DARK("深色"),
}

data class ThemeSettings(
    val colorChoice: ThemeChoice = ThemeChoice.RED,
    val customHue: Float = 350f,
    val displayMode: DisplayMode = DisplayMode.SYSTEM,
)

class ThemePreferences(private val context: Context) {
    private val colorKey = stringPreferencesKey("color_choice")
    private val customHueKey = floatPreferencesKey("custom_hue")
    private val displayModeKey = stringPreferencesKey("display_mode")

    val settings: Flow<ThemeSettings> = context.themeDataStore.data.map { preferences ->
        ThemeSettings(
            colorChoice = enumValueOrDefault(preferences[colorKey], ThemeChoice.RED),
            customHue = preferences[customHueKey] ?: 350f,
            displayMode = enumValueOrDefault(preferences[displayModeKey], DisplayMode.SYSTEM),
        )
    }

    suspend fun save(settings: ThemeSettings) {
        context.themeDataStore.edit { preferences ->
            preferences[colorKey] = settings.colorChoice.name
            preferences[customHueKey] = settings.customHue
            preferences[displayModeKey] = settings.displayMode.name
        }
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: default
}
