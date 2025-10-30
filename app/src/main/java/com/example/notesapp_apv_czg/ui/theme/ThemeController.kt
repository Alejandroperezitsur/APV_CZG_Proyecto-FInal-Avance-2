package com.example.notesapp_apv_czg.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.Calendar
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val KEY_THEME_NAME = stringPreferencesKey("app_theme_name")
private val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode") // preset | custom | dynamic_time | dynamic_season | system

// Custom color keys (store ARGB as Int)
private val KEY_CUSTOM_PRIMARY = intPreferencesKey("custom_primary")
private val KEY_CUSTOM_SECONDARY = intPreferencesKey("custom_secondary")
private val KEY_CUSTOM_TERTIARY = intPreferencesKey("custom_tertiary")
private val KEY_CUSTOM_BACKGROUND = intPreferencesKey("custom_background")
private val KEY_CUSTOM_SURFACE = intPreferencesKey("custom_surface")
private val KEY_CUSTOM_SURFACE_VARIANT = intPreferencesKey("custom_surface_variant")
private val KEY_CUSTOM_ON_PRIMARY = intPreferencesKey("custom_on_primary")
private val KEY_CUSTOM_ON_SECONDARY = intPreferencesKey("custom_on_secondary")
private val KEY_CUSTOM_ON_TERTIARY = intPreferencesKey("custom_on_tertiary")
private val KEY_CUSTOM_ON_BACKGROUND = intPreferencesKey("custom_on_background")
private val KEY_CUSTOM_ON_SURFACE = intPreferencesKey("custom_on_surface")
private val KEY_CUSTOM_OUTLINE = intPreferencesKey("custom_outline")
private val KEY_CUSTOM_ERROR = intPreferencesKey("custom_error")

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class ThemeController(private val context: Context) {
    var currentTheme by mutableStateOf<AppTheme>(ThemePresets.DefaultLight)
        private set

    var currentMode by mutableStateOf("preset")
        private set

    suspend fun load(dark: Boolean) {
        val prefs = context.appDataStore.data.first()
        val mode = prefs[KEY_THEME_MODE] ?: "preset"
        currentMode = mode
        currentTheme = when (mode) {
            "preset" -> {
                val name = prefs[KEY_THEME_NAME]
                presetByName(name, dark)
            }
            "custom" -> readCustomTheme() ?: presetByName(prefs[KEY_THEME_NAME], dark)
            "dynamic_time" -> dynamicTimeTheme()
            "dynamic_season" -> dynamicSeasonTheme()
            "system" -> if (dark) ThemePresets.DefaultDark else ThemePresets.DefaultLight
            else -> if (dark) ThemePresets.DefaultDark else ThemePresets.DefaultLight
        }
    }

    private fun presetByName(name: String?, dark: Boolean): AppTheme {
        return when (name) {
            "Ocean" -> ThemePresets.Ocean
            "Sunset" -> ThemePresets.Sunset
            "Forest" -> ThemePresets.Forest
            "DefaultDark" -> ThemePresets.DefaultDark
            else -> if (dark) ThemePresets.DefaultDark else ThemePresets.DefaultLight
        }
    }

    suspend fun setTheme(name: String) {
        val theme = presetByName(name, dark = false)
        currentTheme = theme
        context.appDataStore.edit {
            it[KEY_THEME_NAME] = name
            it[KEY_THEME_MODE] = "preset"
        }
        currentMode = "preset"
    }

    suspend fun setMode(mode: String, dark: Boolean) {
        context.appDataStore.edit { it[KEY_THEME_MODE] = mode }
        currentMode = mode
        // Refresh current theme under new mode
        when (mode) {
            "preset" -> {
                val prefs = context.appDataStore.data.first()
                currentTheme = presetByName(prefs[KEY_THEME_NAME], dark)
            }
            "custom" -> {
                currentTheme = readCustomTheme() ?: presetByName(null, dark)
            }
            "dynamic_time" -> currentTheme = dynamicTimeTheme()
            "dynamic_season" -> currentTheme = dynamicSeasonTheme()
            "system" -> currentTheme = if (dark) ThemePresets.DefaultDark else ThemePresets.DefaultLight
        }
    }

    suspend fun setCustomTheme(theme: AppTheme) {
        currentTheme = theme
        context.appDataStore.edit {
            it[KEY_THEME_MODE] = "custom"
            it[KEY_CUSTOM_PRIMARY] = theme.primary.toArgb()
            it[KEY_CUSTOM_SECONDARY] = theme.secondary.toArgb()
            it[KEY_CUSTOM_TERTIARY] = theme.tertiary.toArgb()
            it[KEY_CUSTOM_BACKGROUND] = theme.background.toArgb()
            it[KEY_CUSTOM_SURFACE] = theme.surface.toArgb()
            it[KEY_CUSTOM_SURFACE_VARIANT] = theme.surfaceVariant.toArgb()
            it[KEY_CUSTOM_ON_PRIMARY] = theme.onPrimary.toArgb()
            it[KEY_CUSTOM_ON_SECONDARY] = theme.onSecondary.toArgb()
            it[KEY_CUSTOM_ON_TERTIARY] = theme.onTertiary.toArgb()
            it[KEY_CUSTOM_ON_BACKGROUND] = theme.onBackground.toArgb()
            it[KEY_CUSTOM_ON_SURFACE] = theme.onSurface.toArgb()
            it[KEY_CUSTOM_OUTLINE] = theme.outline.toArgb()
            it[KEY_CUSTOM_ERROR] = theme.error.toArgb()
        }
        currentMode = "custom"
    }

    suspend fun resetCustom(dark: Boolean) {
        context.appDataStore.edit {
            it.remove(KEY_CUSTOM_PRIMARY)
            it.remove(KEY_CUSTOM_SECONDARY)
            it.remove(KEY_CUSTOM_TERTIARY)
            it.remove(KEY_CUSTOM_BACKGROUND)
            it.remove(KEY_CUSTOM_SURFACE)
            it.remove(KEY_CUSTOM_SURFACE_VARIANT)
            it.remove(KEY_CUSTOM_ON_PRIMARY)
            it.remove(KEY_CUSTOM_ON_SECONDARY)
            it.remove(KEY_CUSTOM_ON_TERTIARY)
            it.remove(KEY_CUSTOM_ON_BACKGROUND)
            it.remove(KEY_CUSTOM_ON_SURFACE)
            it.remove(KEY_CUSTOM_OUTLINE)
            it.remove(KEY_CUSTOM_ERROR)
        }
        currentTheme = presetByName(null, dark)
    }

    private suspend fun readCustomTheme(): AppTheme? {
        val prefs = context.appDataStore.data.first()
        fun colorOrNull(key: Preferences.Key<Int>): Color? = prefs[key]?.let { Color(it) }
        val primary = colorOrNull(KEY_CUSTOM_PRIMARY) ?: return null
        val secondary = colorOrNull(KEY_CUSTOM_SECONDARY) ?: return null
        val tertiary = colorOrNull(KEY_CUSTOM_TERTIARY) ?: return null
        val background = colorOrNull(KEY_CUSTOM_BACKGROUND) ?: return null
        val surface = colorOrNull(KEY_CUSTOM_SURFACE) ?: return null
        val surfaceVariant = colorOrNull(KEY_CUSTOM_SURFACE_VARIANT) ?: return null
        val onPrimary = colorOrNull(KEY_CUSTOM_ON_PRIMARY) ?: return null
        val onSecondary = colorOrNull(KEY_CUSTOM_ON_SECONDARY) ?: return null
        val onTertiary = colorOrNull(KEY_CUSTOM_ON_TERTIARY) ?: return null
        val onBackground = colorOrNull(KEY_CUSTOM_ON_BACKGROUND) ?: return null
        val onSurface = colorOrNull(KEY_CUSTOM_ON_SURFACE) ?: return null
        val outline = colorOrNull(KEY_CUSTOM_OUTLINE) ?: return null
        val error = colorOrNull(KEY_CUSTOM_ERROR) ?: return null
        return AppTheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            background = background,
            surface = surface,
            surfaceVariant = surfaceVariant,
            onPrimary = onPrimary,
            onSecondary = onSecondary,
            onTertiary = onTertiary,
            onBackground = onBackground,
            onSurface = onSurface,
            outline = outline,
            error = error
        )
    }

    private fun dynamicTimeTheme(): AppTheme {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> ThemePresets.Sunrise
            in 11..17 -> ThemePresets.Ocean
            in 18..20 -> ThemePresets.Sunset
            else -> ThemePresets.Midnight
        }
    }

    private fun dynamicSeasonTheme(): AppTheme {
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        return when (month) {
            in 3..5 -> ThemePresets.Spring
            in 6..8 -> ThemePresets.Summer
            in 9..11 -> ThemePresets.Autumn
            else -> ThemePresets.Winter
        }
    }
}

@Composable
fun rememberThemeController(context: Context, dark: Boolean): ThemeController {
    val controller = remember { ThemeController(context) }
    LaunchedEffect(dark) { controller.load(dark) }
    return controller
}