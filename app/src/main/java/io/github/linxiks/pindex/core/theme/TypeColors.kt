package io.github.linxiks.pindex.core.theme

import androidx.compose.ui.graphics.Color

data class TypeColor(val container: Color, val content: Color)

private val DarkText = Color(0xFF1F2329)
private val LightText = Color(0xFFFFFFFF)

private class TypePalette(val light: TypeColor, val dark: TypeColor)

private fun palette(light: Long, dark: Long, content: Color) =
    TypePalette(TypeColor(Color(light), content), TypeColor(Color(dark), content))

// Keyed by type identifier; unknown identifiers (stellar, future types) use DefaultPalette.
private val Palettes: Map<String, TypePalette> = mapOf(
    "normal" to palette(0xFFA8A77A, 0xFF93936D, DarkText),
    "fire" to palette(0xFFEE8130, 0xFFCF722E, DarkText),
    "water" to palette(0xFF6390F0, 0xFF608AE6, DarkText),
    "electric" to palette(0xFFF7D02C, 0xFFD6B62B, DarkText),
    "grass" to palette(0xFF7AC74C, 0xFF6CAE46, DarkText),
    "ice" to palette(0xFF96D9D6, 0xFF84BDBB, DarkText),
    "fighting" to palette(0xFFC22E28, 0xFFA92C27, LightText),
    "poison" to palette(0xFFA33EA1, 0xFF8F3A8E, LightText),
    "ground" to palette(0xFFE2BF65, 0xFFC5A75B, DarkText),
    "flying" to palette(0xFFA98FF3, 0xFF947ED4, DarkText),
    "psychic" to palette(0xFFF95587, 0xFFEE5282, DarkText),
    "bug" to palette(0xFFA6B91A, 0xFF92A21C, DarkText),
    "rock" to palette(0xFFB6A136, 0xFF9F8E33, DarkText),
    "ghost" to palette(0xFF735797, 0xFF664F86, LightText),
    "dragon" to palette(0xFF6F35FC, 0xFF6332DC, LightText),
    "dark" to palette(0xFF5B5466, 0xFF746E7D, LightText),
    "steel" to palette(0xFFB7B7CE, 0xFFA0A0B4, DarkText),
    "fairy" to palette(0xFFD685AD, 0xFFBA7698, DarkText),
)

private val DefaultPalette = palette(0xFF9E9E9E, 0xFF8B8B8C, DarkText)

fun typeColor(identifier: String, dark: Boolean): TypeColor {
    val p = Palettes[identifier] ?: DefaultPalette
    return if (dark) p.dark else p.light
}
