package com.example.notesapp_apv_czg.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf

object ThemeManager {
    private var currentScheme = mutableStateOf(predefinedSchemes[0])
    
    fun getCurrentScheme(): ColorSchemeOption = currentScheme.value
    
    fun setColorScheme(scheme: ColorSchemeOption) {
        currentScheme.value = scheme
    }
    
    @Composable
    fun getColorScheme(): ColorScheme {
        val scheme = currentScheme.value
        return lightColorScheme(
            primary = scheme.primary,
            secondary = scheme.secondary,
            tertiary = scheme.tertiary,
            surface = scheme.surface,
            background = scheme.background
        )
    }
}

// Local composition para acceder al ThemeManager desde cualquier parte de la app
val LocalThemeManager = staticCompositionLocalOf { ThemeManager }