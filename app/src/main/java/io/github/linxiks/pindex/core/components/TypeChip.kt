package io.github.linxiks.pindex.core.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.linxiks.pindex.core.theme.LocalDarkTheme
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.core.theme.typeColor

/** A type as shown in the UI: localized name plus identifier for the color lookup. */
data class TypeUi(val name: String, val identifier: String)

/** Type label; the Chinese name is always shown so color is never the only signal. */
@Composable
fun TypeChip(name: String, identifier: String, modifier: Modifier = Modifier) {
    val colors = typeColor(identifier, LocalDarkTheme.current)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = colors.container,
        contentColor = colors.content,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.xs),
        )
    }
}

@PreviewLightDark
@Composable
private fun TypeChipPreview() {
    PindexTheme {
        Surface { TypeChip(name = "电", identifier = "electric") }
    }
}
