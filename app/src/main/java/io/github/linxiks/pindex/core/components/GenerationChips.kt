package io.github.linxiks.pindex.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.data.model.Generation
import io.github.linxiks.pindex.domain.toRoman
import io.github.linxiks.pindex.ui.preview.SampleData

/** [label] is the short chip text (Ⅰ); [description] is the spoken name (第一世代). */
data class GenerationUi(val id: Int, val label: String, val description: String)

fun Generation.toUi(): GenerationUi = GenerationUi(id, toRoman(id), name.text)

/** Generation filter row; id 0 means all generations. */
@Composable
fun GenerationChips(
    generations: List<GenerationUi>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        item(key = 0) {
            FilterChip(
                selected = selected == 0,
                onClick = { onSelect(0) },
                label = { Text(stringResource(R.string.generation_all)) },
                shape = MaterialTheme.shapes.small,
            )
        }
        items(generations, key = { it.id }) { generation ->
            FilterChip(
                selected = selected == generation.id,
                onClick = { onSelect(generation.id) },
                label = { Text(generation.label) },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.semantics { contentDescription = generation.description },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun GenerationChipsPreview() {
    PindexTheme {
        Surface { GenerationChips(SampleData.generations.map { it.toUi() }, selected = 1, onSelect = {}) }
    }
}
