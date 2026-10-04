package io.github.linxiks.pindex.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val LightSurface = Color(0xFFFFFFFF)
private val DarkSurface = Color(0xFF1E2024)
private val LightOn = Color(0xFF1F2329)
private val DarkOn = Color(0xFFE4E6E9)

val LightColors = lightColorScheme(
    background = Color(0xFFF5F6F8),
    surface = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainerHigh = Color(0xFFEBEDF0),
    surfaceVariant = Color(0xFFECEEF1),
    onBackground = LightOn,
    onSurface = LightOn,
    onSurfaceVariant = Color(0xFF5F6670),
    outlineVariant = Color(0xFFE1E4E8),
    outline = Color(0xFF8A919B),
    primary = Color(0xFF2B5FD9),
    onPrimary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE5F9),
    onSecondaryContainer = LightOn,
)

val DarkColors = darkColorScheme(
    background = Color(0xFF141618),
    surface = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerLow = DarkSurface,
    surfaceContainerHigh = Color(0xFF282B2F),
    surfaceVariant = Color(0xFF2A2D31),
    onBackground = DarkOn,
    onSurface = DarkOn,
    onSurfaceVariant = Color(0xFFA3A9B1),
    outlineVariant = Color(0xFF33373C),
    outline = Color(0xFF6E747C),
    primary = Color(0xFF8AB0FF),
    onPrimary = Color(0xFF0B2A66),
    secondaryContainer = Color(0xFF2B3A57),
    onSecondaryContainer = DarkOn,
)
