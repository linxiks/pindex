package io.github.linxiks.pindex.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/** Whether the active PindexTheme is dark; drives [typeColor]. */
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun PindexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = PindexTypography,
            shapes = PindexShapes,
            content = content,
        )
    }
}
