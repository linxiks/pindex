package io.github.linxiks.pindex.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.components.EmptyState
import io.github.linxiks.pindex.core.components.SearchField
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.ui.PokemonGrid
import io.github.linxiks.pindex.ui.preview.SampleData

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchContent(
        query = query,
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onBack = onBack,
        onOpenPokemon = onOpenPokemon,
    )
}

@Composable
fun SearchContent(
    query: String,
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    // Focus only on first entry, not every time the user comes back from a detail page.
    var autoFocused by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!autoFocused) {
            focusRequester.requestFocus()
            autoFocused = true
        }
    }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(R.string.search_placeholder),
            onBack = onBack,
            focusRequester = focusRequester,
            modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l),
        )
        when (state) {
            SearchUiState.Idle -> EmptyState(
                title = stringResource(R.string.search_idle_title),
                message = stringResource(R.string.search_idle_message),
            )
            SearchUiState.Empty -> EmptyState(
                title = stringResource(R.string.search_empty_title),
                message = stringResource(R.string.search_empty_message),
            )
            SearchUiState.Error -> EmptyState(
                title = stringResource(R.string.load_error_title),
                message = stringResource(R.string.load_error_message),
            )
            is SearchUiState.Content -> PokemonGrid(items = state.items, onOpenPokemon = onOpenPokemon)
        }
    }
}

@PreviewLightDark
@Composable
private fun SearchContentPreview() {
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SearchContent(
                query = "皮卡",
                state = SearchUiState.Content(SampleData.pokemon.filter { it.speciesId == 25 }),
                onQueryChange = {},
                onBack = {},
                onOpenPokemon = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SearchEmptyPreview() {
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SearchContent("zzzz", SearchUiState.Empty, {}, {}, {})
        }
    }
}
