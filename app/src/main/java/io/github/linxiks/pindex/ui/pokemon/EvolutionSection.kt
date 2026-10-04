package io.github.linxiks.pindex.ui.pokemon

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.components.EvolutionNode
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.ui.preview.SampleData
import io.github.linxiks.pindex.data.model.EvolutionNode as EvolutionNodeModel

/**
 * Evolution tree drawn top-down: each branch shows its condition, then the species, then its
 * children side by side. Linear chains are centred; wide trees (Eevee) scroll horizontally.
 */
@Composable
fun EvolutionSection(roots: List<EvolutionNodeModel>, currentSpeciesId: Int, onOpenPokemon: (Int) -> Unit) {
    if (roots.size == 1 && roots.single().children.isEmpty()) {
        Text(
            text = stringResource(R.string.evolution_none),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).widthIn(min = maxWidth),
            horizontalArrangement = Arrangement.spacedBy(Spacing.l, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Top,
        ) {
            roots.forEach { Branch(it, currentSpeciesId, onOpenPokemon) }
        }
    }
}

@Composable
private fun Branch(node: EvolutionNodeModel, currentSpeciesId: Int, onOpenPokemon: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        node.condition?.let { condition ->
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
            Text(
                text = condition,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 112.dp),
            )
        }
        val isCurrent = node.speciesId == currentSpeciesId
        EvolutionNode(
            name = node.name,
            speciesId = node.speciesId,
            isCurrent = isCurrent,
            onClick = if (isCurrent) null else { { onOpenPokemon(node.pokemonId) } },
        )
        if (node.children.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m), verticalAlignment = Alignment.Top) {
                node.children.forEach { Branch(it, currentSpeciesId, onOpenPokemon) }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun EvolutionSectionPreview() {
    PindexTheme {
        Surface {
            EvolutionSection(SampleData.pikachu.evolution, currentSpeciesId = 25, onOpenPokemon = {})
        }
    }
}
