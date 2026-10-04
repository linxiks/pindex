package io.github.linxiks.pindex.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing

/** Two-column label/value grid; an odd last item leaves the right cell empty. Values may carry fallback markers. */
@Composable
fun InfoGrid(items: List<Pair<String, AnnotatedString>>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
                row.forEach { (label, value) -> InfoCell(label, value, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoCell(label: String, value: AnnotatedString, modifier: Modifier) {
    // Merge so TalkBack reads "label, value" as one item.
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@PreviewLightDark
@Composable
private fun InfoGridPreview() {
    PindexTheme {
        Surface {
            InfoGrid(
                items = listOf("分类" to "鼠宝可梦", "日文名" to "ピカチュウ", "身高" to "0.4 m")
                    .map { (label, value) -> label to AnnotatedString(value) },
                modifier = Modifier.padding(Spacing.l),
            )
        }
    }
}
