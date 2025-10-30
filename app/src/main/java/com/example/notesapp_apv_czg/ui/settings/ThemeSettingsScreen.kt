@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.notesapp_apv_czg.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.notesapp_apv_czg.ui.theme.ThemePresets
import com.example.notesapp_apv_czg.ui.theme.AppTheme
import kotlin.math.pow
import kotlin.math.max

@Composable
fun ThemeSettingsScreen(
    currentTheme: AppTheme,
    currentMode: String,
    onSetMode: (String) -> Unit,
    onSelectPreset: (String) -> Unit,
    onSetCustom: (AppTheme) -> Unit,
    onResetCustom: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Tema y Colores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Palette, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Modo de tema", style = MaterialTheme.typography.titleMedium)
            ModeRow(currentMode = currentMode, onSetMode = onSetMode)

            if (currentMode == "preset") {
                Text(text = "Selecciona un tema", style = MaterialTheme.typography.titleMedium)
                PresetRow(
                    label = "Ligero (Por defecto)",
                    colors = listOf(ThemePresets.DefaultLight.primary, ThemePresets.DefaultLight.tertiary),
                    selected = currentTheme == ThemePresets.DefaultLight,
                    onClick = { onSelectPreset("DefaultLight") }
                )
                PresetRow(
                    label = "Oscuro (Por defecto)",
                    colors = listOf(ThemePresets.DefaultDark.primary, ThemePresets.DefaultDark.tertiary),
                    selected = currentTheme == ThemePresets.DefaultDark,
                    onClick = { onSelectPreset("DefaultDark") }
                )
                PresetRow(
                    label = "Océano",
                    colors = listOf(ThemePresets.Ocean.primary, ThemePresets.Ocean.secondary),
                    selected = currentTheme == ThemePresets.Ocean,
                    onClick = { onSelectPreset("Ocean") }
                )
                PresetRow(
                    label = "Amanecer",
                    colors = listOf(ThemePresets.Sunrise.primary, ThemePresets.Sunrise.secondary),
                    selected = currentTheme == ThemePresets.Sunrise,
                    onClick = { onSelectPreset("Sunrise") }
                )
                PresetRow(
                    label = "Atardecer",
                    colors = listOf(ThemePresets.Sunset.primary, ThemePresets.Sunset.secondary),
                    selected = currentTheme == ThemePresets.Sunset,
                    onClick = { onSelectPreset("Sunset") }
                )
                PresetRow(
                    label = "Medianoche",
                    colors = listOf(ThemePresets.Midnight.primary, ThemePresets.Midnight.secondary),
                    selected = currentTheme == ThemePresets.Midnight,
                    onClick = { onSelectPreset("Midnight") }
                )
                PresetRow(
                    label = "Bosque",
                    colors = listOf(ThemePresets.Forest.primary, ThemePresets.Forest.secondary),
                    selected = currentTheme == ThemePresets.Forest,
                    onClick = { onSelectPreset("Forest") }
                )
                PresetRow(
                    label = "Invierno",
                    colors = listOf(ThemePresets.Winter.primary, ThemePresets.Winter.secondary),
                    selected = currentTheme == ThemePresets.Winter,
                    onClick = { onSelectPreset("Winter") }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onSelectPreset(listOf("DefaultLight","DefaultDark","Ocean","Sunrise","Sunset","Midnight","Forest","Winter").random()) }) {
                    Text("Sorpréndeme")
                }
            }

            if (currentMode == "custom") {
                Text(text = "Personaliza tus colores", style = MaterialTheme.typography.titleMedium)
                var primary by remember { mutableStateOf(currentTheme.primary) }
                var secondary by remember { mutableStateOf(currentTheme.secondary) }
                var tertiary by remember { mutableStateOf(currentTheme.tertiary) }
                var background by remember { mutableStateOf(currentTheme.background) }
                var surface by remember { mutableStateOf(currentTheme.surface) }

                ColorEditor("Primario", primary, onPick = { primary = it })
                ColorEditor("Secundario", secondary, onPick = { secondary = it })
                ColorEditor("Terciario", tertiary, onPick = { tertiary = it })
                ColorEditor("Fondo", background, onPick = { background = it })
                ColorEditor("Superficie", surface, onPick = { surface = it })

                val onPrimary = bestOnColor(primary)
                val onSecondary = bestOnColor(secondary)
                val onTertiary = bestOnColor(tertiary)
                val onBackground = bestOnColor(background)
                val onSurface = bestOnColor(surface)

                val ratio = contrastRatio(onBackground, background)
                val warnLowContrast = ratio < 4.5
                if (warnLowContrast) {
                    Text(
                        text = "Aviso: Contraste bajo (ratio %.2f) en fondo".format(ratio),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                LivePreview(
                    theme = AppTheme(
                        primary = primary,
                        secondary = secondary,
                        tertiary = tertiary,
                        background = background,
                        surface = surface,
                        surfaceVariant = currentTheme.surfaceVariant,
                        onPrimary = onPrimary,
                        onSecondary = onSecondary,
                        onTertiary = onTertiary,
                        onBackground = onBackground,
                        onSurface = onSurface,
                        outline = currentTheme.outline,
                        error = currentTheme.error
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        onSetCustom(
                            AppTheme(
                                primary = primary,
                                secondary = secondary,
                                tertiary = tertiary,
                                background = background,
                                surface = surface,
                                surfaceVariant = currentTheme.surfaceVariant,
                                onPrimary = onPrimary,
                                onSecondary = onSecondary,
                                onTertiary = onTertiary,
                                onBackground = onBackground,
                                onSurface = onSurface,
                                outline = currentTheme.outline,
                                error = currentTheme.error
                            )
                        )
                    }) { Text("Guardar colores") }

                    OutlinedButton(onClick = { onResetCustom() }) { Text("Reset") }
                }
            }
        }
    }
}

@Composable
private fun PresetRow(
    label: String,
    colors: List<Color>,
    selected: Boolean,
    onClick: () -> Unit
) {
    val gradient = remember(colors) {
        Brush.horizontalGradient(colors)
    }
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier
                .height(36.dp)
                .weight(1f)) {
                drawRect(brush = gradient)
            }
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ModeRow(currentMode: String, onSetMode: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val modes = listOf(
            "preset" to "Predefinidos",
            "custom" to "Personalizado",
            "dynamic_time" to "Dinámico (hora)",
            "dynamic_season" to "Dinámico (estación)",
            "system" to "Sistema"
        )
        modes.forEach { (value, label) ->
            AssistChip(
                onClick = { onSetMode(value) },
                label = { Text(label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (currentMode == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    labelColor = if (currentMode == value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun ColorPickerRow(label: String, current: Color, onPick: (Color) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val palette = listOf(
                ThemePresets.DefaultLight.primary,
                ThemePresets.DefaultDark.primary,
                ThemePresets.Ocean.primary,
                ThemePresets.Sunrise.primary,
                ThemePresets.Sunset.primary,
                ThemePresets.Midnight.primary,
                ThemePresets.Forest.primary,
                ThemePresets.Winter.primary
            )
            palette.forEach { c ->
                ColorSwatch(color = c, selected = c == current) { onPick(c) }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ColorEditor(label: String, current: Color, onPick: (Color) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        ColorPickerRow(label, current, onPick)
        var hex by remember { mutableStateOf(colorToHex(current)) }
        OutlinedTextField(
            value = hex,
            onValueChange = { value ->
                hex = value
                parseHexColor(value)?.let { onPick(it) }
            },
            label = { Text("Hex (#RRGGBB)") },
            singleLine = true,
            visualTransformation = VisualTransformation.None
        )
    }
}

private fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    val rgb = argb and 0x00FFFFFF
    return "#%06X".format(rgb)
}

private fun parseHexColor(input: String): Color? {
    val s = input.trim().removePrefix("#")
    if (s.length != 6) return null
    return try {
        val rgb = s.toInt(16)
        val argb = 0xFF000000.toInt() or rgb
        Color(argb)
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun LivePreview(theme: AppTheme) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = theme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header sample
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Text(text = "Header", color = theme.onSurface)
                    Button(onClick = {}, enabled = false, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary)) {
                        Text("Acción")
                    }
                }
            }

            // Body sample
            Text(text = "Texto de ejemplo", color = theme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, enabled = false, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary)) { Text("Primario") }
                OutlinedButton(onClick = {}, enabled = false) { Text("Secundario", color = theme.secondary) }
            }
        }
    }
}

private fun relativeLuminance(color: Color): Double {
    fun channel(c: Float): Double {
        val v = c.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }
    val r = channel(color.red)
    val g = channel(color.green)
    val b = channel(color.blue)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

private fun contrastRatio(foreground: Color, background: Color): Double {
    val l1 = relativeLuminance(foreground)
    val l2 = relativeLuminance(background)
    val (lighter, darker) = if (l1 >= l2) l1 to l2 else l2 to l1
    return (lighter + 0.05) / (darker + 0.05)
}

private fun bestOnColor(color: Color): Color {
    return if (relativeLuminance(color) < 0.5) Color.White else Color.Black
}