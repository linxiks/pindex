package io.github.linxiks.pindex.ui.pokedex

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.components.EmptyState
import io.github.linxiks.pindex.core.components.GenerationChips
import io.github.linxiks.pindex.core.components.SearchEntry
import io.github.linxiks.pindex.core.components.toUi
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.ui.PokemonGrid
import io.github.linxiks.pindex.ui.preview.SampleData
import kotlinx.coroutines.launch

@Composable
fun PokedexScreen(
    onOpenSearch: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
    viewModel: PokedexViewModel = viewModel(factory = PokedexViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PokedexContent(
        state = state,
        onSelectGeneration = viewModel::selectGeneration,
        onOpenSearch = onOpenSearch,
        onOpenPokemon = onOpenPokemon,
    )
}

/** Fixed header (title, search entry, generation chips) above a scrolling card grid. */
@Composable
fun PokedexContent(
    state: PokedexUiState,
    onSelectGeneration: (Int) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
) {
    // Hoisted here (saveable) so the scroll position survives detail round trips and tab switches.
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            text = stringResource(R.string.pokedex_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l, bottom = Spacing.m),
        )
        SearchEntry(
            placeholder = stringResource(R.string.search_placeholder),
            onClick = onOpenSearch,
            modifier = Modifier.padding(horizontal = Spacing.l),
        )
        when (state) {
            PokedexUiState.Error -> EmptyState(
                title = stringResource(R.string.load_error_title),
                message = stringResource(R.string.load_error_message),
            )
            PokedexUiState.Loading -> PokemonGrid(items = null, onOpenPokemon = onOpenPokemon, state = gridState)
            is PokedexUiState.Content -> {
                GenerationChips(
                    generations = state.generations.map { it.toUi() },
                    selected = state.selectedGeneration,
                    onSelect = { id ->
                        if (id != state.selectedGeneration) {
                            onSelectGeneration(id)
                            scope.launch { gridState.scrollToItem(0) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                    contentPadding = PaddingValues(horizontal = Spacing.l),
                )
                PokemonGrid(items = state.items, onOpenPokemon = onOpenPokemon, state = gridState)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PokedexContentPreview() {
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PokedexContent(
                state = PokedexUiState.Content(SampleData.pokemon, SampleData.generations, selectedGeneration = 0),
                onSelectGeneration = {},
                onOpenSearch = {},
                onOpenPokemon = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PokedexLoadingPreview() {
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PokedexContent(PokedexUiState.Loading, {}, {}, {})
        }
    }
}
