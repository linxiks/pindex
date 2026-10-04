package io.github.linxiks.pindex.ui.search

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import io.github.linxiks.pindex.core.components.AbilityItem
import io.github.linxiks.pindex.core.components.EmptyState
import io.github.linxiks.pindex.core.components.MoveItem
import io.github.linxiks.pindex.core.components.SearchField
import io.github.linxiks.pindex.core.components.SectionHeader
import io.github.linxiks.pindex.core.components.TypeUi
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.data.model.AbilitySummary
import io.github.linxiks.pindex.data.model.MoveSummary
import io.github.linxiks.pindex.domain.LocalizedText
import io.github.linxiks.pindex.ui.PokemonGrid
import io.github.linxiks.pindex.ui.preview.SampleData

/** Pokemon cards shown in the All view: three rows of two. */
private const val ALL_POKEMON_PREVIEW = 6

/** Move and ability rows shown per group in the All view. */
private const val ALL_ROW_PREVIEW = 5

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
    onOpenMove: (Int) -> Unit,
    onOpenAbility: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    SearchContent(
        query = query,
        state = state,
        category = category,
        onQueryChange = viewModel::onQueryChange,
        onSelectCategory = viewModel::selectCategory,
        onBack = onBack,
        onOpenPokemon = onOpenPokemon,
        onOpenMove = onOpenMove,
        onOpenAbility = onOpenAbility,
    )
}

@Composable
fun SearchContent(
    query: String,
    state: SearchUiState,
    category: SearchCategory,
    onQueryChange: (String) -> Unit,
    onSelectCategory: (SearchCategory) -> Unit,
    onBack: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
    onOpenMove: (Int) -> Unit,
    onOpenAbility: (Int) -> Unit,
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
            is SearchUiState.Content -> SearchResultsContent(
                results = state.results,
                category = category,
                onSelectCategory = onSelectCategory,
                onOpenPokemon = onOpenPokemon,
                onOpenMove = onOpenMove,
                onOpenAbility = onOpenAbility,
            )
        }
    }
}

/** Category chips, then one grid: pokemon cards plus full-width move and ability rows. */
@Composable
private fun SearchResultsContent(
    results: SearchResults,
    category: SearchCategory,
    onSelectCategory: (SearchCategory) -> Unit,
    onOpenPokemon: (Int) -> Unit,
    onOpenMove: (Int) -> Unit,
    onOpenAbility: (Int) -> Unit,
) {
    val categories = results.categories()
    // Falls back only for display; the user's choice is kept for the next query.
    val shown = category.takeIf { it in categories } ?: SearchCategory.All
    val all = shown == SearchCategory.All
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = Spacing.l, end = Spacing.l, top = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        categories.forEach { c ->
            FilterChip(
                selected = c == shown,
                onClick = { onSelectCategory(c) },
                label = { Text(stringResource(categoryLabel(c))) },
                shape = MaterialTheme.shapes.small,
            )
        }
    }
    val cards = when (shown) {
        SearchCategory.All -> results.pokemon.take(ALL_POKEMON_PREVIEW)
        SearchCategory.Pokemon -> results.pokemon
        else -> emptyList()
    }
    PokemonGrid(
        items = cards,
        onOpenPokemon = onOpenPokemon,
        header = {
            if (all && results.pokemon.isNotEmpty()) groupHeader("header-pokemon", SearchCategory.Pokemon)
        },
        footer = {
            if (all) {
                if (results.pokemon.size > ALL_POKEMON_PREVIEW) {
                    showAll("more-pokemon", results.pokemon.size) { onSelectCategory(SearchCategory.Pokemon) }
                }
                if (results.moves.isNotEmpty()) {
                    groupHeader("header-move", SearchCategory.Move)
                    moveRows(results.moves.take(ALL_ROW_PREVIEW), onOpenMove)
                    if (results.moves.size > ALL_ROW_PREVIEW) {
                        showAll("more-move", results.moves.size) { onSelectCategory(SearchCategory.Move) }
                    }
                }
                if (results.abilities.isNotEmpty()) {
                    groupHeader("header-ability", SearchCategory.Ability)
                    abilityRows(results.abilities.take(ALL_ROW_PREVIEW), onOpenAbility)
                    if (results.abilities.size > ALL_ROW_PREVIEW) {
                        showAll("more-ability", results.abilities.size) { onSelectCategory(SearchCategory.Ability) }
                    }
                }
            } else if (shown == SearchCategory.Move) {
                moveRows(results.moves, onOpenMove)
            } else if (shown == SearchCategory.Ability) {
                abilityRows(results.abilities, onOpenAbility)
            }
        },
    )
}

// Full-line items use String keys, so they never collide with the Int keys of pokemon cards.

private fun LazyGridScope.groupHeader(key: String, category: SearchCategory) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(stringResource(categoryLabel(category)))
    }
}

private fun LazyGridScope.showAll(key: String, count: Int, onClick: () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) {
        TextButton(onClick = onClick) { Text(stringResource(R.string.search_show_all, count)) }
    }
}

private fun LazyGridScope.moveRows(moves: List<MoveSummary>, onOpenMove: (Int) -> Unit) {
    moves.forEach { m ->
        item(key = "move-${m.moveId}", span = { GridItemSpan(maxLineSpan) }) {
            MoveItem(
                leading = null,
                name = m.name,
                type = TypeUi(m.type.name.text, m.type.identifier),
                damageClass = m.damageClass.text,
                power = m.power,
                onClick = { onOpenMove(m.moveId) },
            )
        }
    }
}

private fun LazyGridScope.abilityRows(abilities: List<AbilitySummary>, onOpenAbility: (Int) -> Unit) {
    abilities.forEach { a ->
        item(key = "ability-${a.abilityId}", span = { GridItemSpan(maxLineSpan) }) {
            AbilityItem(name = a.name, effect = a.effect, isHidden = false, onClick = { onOpenAbility(a.abilityId) })
        }
    }
}

@StringRes
private fun categoryLabel(c: SearchCategory): Int = when (c) {
    SearchCategory.All -> R.string.search_category_all
    SearchCategory.Pokemon -> R.string.search_category_pokemon
    SearchCategory.Move -> R.string.search_category_move
    SearchCategory.Ability -> R.string.search_category_ability
}

@PreviewLightDark
@Composable
private fun SearchContentPreview() {
    val thunderbolt = SampleData.thunderbolt
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SearchContent(
                query = "电",
                state = SearchUiState.Content(
                    SearchResults(
                        pokemon = SampleData.pokemon.filter { it.speciesId == 25 },
                        moves = listOf(
                            MoveSummary(thunderbolt.moveId, thunderbolt.name, thunderbolt.type, thunderbolt.damageClass, thunderbolt.power),
                        ),
                        abilities = listOf(
                            AbilitySummary(
                                31,
                                LocalizedText("避雷针", "zh-Hans"),
                                LocalizedText("将电属性的招式吸引到自己身上，不会受到伤害，而是会提高特攻。", "zh-Hans"),
                            ),
                        ),
                    ),
                ),
                category = SearchCategory.All,
                onQueryChange = {},
                onSelectCategory = {},
                onBack = {},
                onOpenPokemon = {},
                onOpenMove = {},
                onOpenAbility = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SearchEmptyPreview() {
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SearchContent("zzzz", SearchUiState.Empty, SearchCategory.All, {}, {}, {}, {}, {}, {})
        }
    }
}
