package com.example.notesapp_apv_czg.ui.theme

import androidx.compose.ui.graphics.Color

data class AppTheme(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onTertiary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val outline: Color,
    val error: Color
)

object ThemePresets {
    val DefaultLight = AppTheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE),
        surfaceVariant = Color(0xFFE7E0EC),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        outline = Color(0xFF79747E),
        error = Color(0xFFB00020)
    )

    val DefaultDark = AppTheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
        background = Color(0xFF1C1B1F),
        surface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFF49454F),
        onPrimary = Color(0xFF2B1D44),
        onSecondary = Color(0xFF1D1A22),
        onTertiary = Color(0xFF2B1A1F),
        onBackground = Color(0xFFE6E1E5),
        onSurface = Color(0xFFE6E1E5),
        outline = Color(0xFF938F99),
        error = Color(0xFFCF6679)
    )

    val Ocean = AppTheme(
        primary = PeterRiver500,
        secondary = Teal500,
        tertiary = Teal200,
        background = Clouds,
        surface = Clouds,
        surfaceVariant = Color(0xFFD6EAF8),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color(0xFF0D3C45),
        onBackground = Color(0xFF0D3C45),
        onSurface = Color(0xFF0D3C45),
        outline = Color(0xFF5D6D7E),
        error = Alizarin500
    )

    val Sunset = AppTheme(
        primary = Sunflower500,
        secondary = Carrot500,
        tertiary = Pink80,
        background = Color(0xFFFFF8E1),
        surface = Color(0xFFFFF8E1),
        surfaceVariant = Color(0xFFFFE0B2),
        onPrimary = Color(0xFF3E2723),
        onSecondary = Color.White,
        onTertiary = Color(0xFF3E2723),
        onBackground = Color(0xFF3E2723),
        onSurface = Color(0xFF3E2723),
        outline = Color(0xFF8D6E63),
        error = Alizarin500
    )

    val Forest = AppTheme(
        primary = Nephritis,
        secondary = Emerald500,
        tertiary = Emerald200,
        background = MidnightBlue,
        surface = MidnightBlue,
        surfaceVariant = Concrete,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color(0xFF0B2E1D),
        onBackground = Clouds,
        onSurface = Clouds,
        outline = Concrete,
        error = Alizarin200
    )

    // Dynamic time presets
    val Sunrise = AppTheme(
        primary = Color(0xFFFFA726), // Orange 400
        secondary = Pink80,
        tertiary = Sunflower200,
        background = Color(0xFFFFF3E0),
        surface = Color(0xFFFFF3E0),
        surfaceVariant = Color(0xFFFFE0B2),
        onPrimary = Color(0xFF3E2723),
        onSecondary = Color(0xFF311B92),
        onTertiary = Color(0xFF3E2723),
        onBackground = Color(0xFF3E2723),
        onSurface = Color(0xFF3E2723),
        outline = Color(0xFF8D6E63),
        error = Alizarin500
    )

    val Midnight = AppTheme(
        primary = Wisteria,
        secondary = PeterRiver500,
        tertiary = PeterRiver200,
        background = MidnightBlue,
        surface = MidnightBlue,
        surfaceVariant = Concrete,
        onPrimary = Clouds,
        onSecondary = Clouds,
        onTertiary = Clouds,
        onBackground = Clouds,
        onSurface = Clouds,
        outline = Concrete,
        error = Alizarin500
    )

    // Seasonal presets
    val Spring = AppTheme(
        primary = Emerald500,
        secondary = PeterRiver200,
        tertiary = Pink80,
        background = Clouds,
        surface = Clouds,
        surfaceVariant = Color(0xFFE8F6F3),
        onPrimary = Color.White,
        onSecondary = Color(0xFF0D3C45),
        onTertiary = Color(0xFF4A148C),
        onBackground = Color(0xFF0D3C45),
        onSurface = Color(0xFF0D3C45),
        outline = Color(0xFF5D6D7E),
        error = Alizarin500
    )

    val Summer = AppTheme(
        primary = Sunflower500,
        secondary = Carrot500,
        tertiary = Teal200,
        background = Color(0xFFFFFDE7),
        surface = Color(0xFFFFFDE7),
        surfaceVariant = Color(0xFFFFF59D),
        onPrimary = Color(0xFF3E2723),
        onSecondary = Color.White,
        onTertiary = Color(0xFF1B5E20),
        onBackground = Color(0xFF3E2723),
        onSurface = Color(0xFF3E2723),
        outline = Color(0xFF8D6E63),
        error = Alizarin500
    )

    val Autumn = AppTheme(
        primary = Carrot500,
        secondary = Sunflower500,
        tertiary = Alizarin200,
        background = Color(0xFFFFF3E0),
        surface = Color(0xFFFFF3E0),
        surfaceVariant = Color(0xFFFFE0B2),
        onPrimary = Color.White,
        onSecondary = Color(0xFF3E2723),
        onTertiary = Color(0xFF3E2723),
        onBackground = Color(0xFF3E2723),
        onSurface = Color(0xFF3E2723),
        outline = Color(0xFF8D6E63),
        error = Alizarin500
    )

    val Winter = AppTheme(
        primary = PeterRiver500,
        secondary = Purple40,
        tertiary = PeterRiver200,
        background = Clouds,
        surface = Clouds,
        surfaceVariant = Color(0xFFD6EAF8),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color(0xFF0D3C45),
        onBackground = Color(0xFF0D3C45),
        onSurface = Color(0xFF0D3C45),
        outline = Color(0xFF5D6D7E),
        error = Alizarin500
    )
}