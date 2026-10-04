package io.github.linxiks.pindex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.linxiks.pindex.core.components.PokemonCard
import io.github.linxiks.pindex.core.components.TypeUi
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.data.model.PokemonListItem
import io.github.linxiks.pindex.domain.formatNumber

private const val SKELETON_COUNT = 6

/**
 * Two-column pokemon card grid shared by the pokedex and search pages.
 * [items] == null shows skeleton cards in the same grid, so [state] survives the Loading → Content switch.
 * [header] items come first; give them full-line spans.
 * [footer] items come after the cards; give them full-line spans.
 */
@Composable
fun PokemonGrid(
    items: List<PokemonListItem>?,
    onOpenPokemon: (Int) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    header: LazyGridScope.() -> Unit = {},
    footer: LazyGridScope.() -> Unit = {},
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = state,
        modifier = modifier,
        contentPadding = PaddingValues(Spacing.l),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        header()
        if (items == null) {
            items(SKELETON_COUNT) { SkeletonCard() }
        } else {
            items(items, key = { it.pokemonId }) { item ->
                PokemonCard(
                    number = formatNumber(item.speciesId),
                    name = item.name.text,
                    types = item.types.map { TypeUi(it.name.text, it.identifier) },
                    onClick = { onOpenPokemon(item.pokemonId) },
                )
            }
        }
        footer()
    }
}

/** Same footprint as [PokemonCard], filled with flat surfaceVariant blocks (no spinner). */
@Composable
private fun SkeletonCard() {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Block(width = 40.dp, height = 14.dp, modifier = Modifier.align(Alignment.Start))
            Box(
                Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(Modifier.height(Spacing.s))
            Block(width = 72.dp, height = 20.dp)
            Spacer(Modifier.height(Spacing.xs))
            Block(width = 48.dp, height = 26.dp)
        }
    }
}

@Composable
private fun Block(width: Dp, height: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(width)
            .height(height)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
