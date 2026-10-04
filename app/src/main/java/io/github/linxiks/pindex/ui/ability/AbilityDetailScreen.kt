package io.github.linxiks.pindex.ui.ability

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.components.EmptyState
import io.github.linxiks.pindex.core.components.LocalizedLabel
import io.github.linxiks.pindex.core.components.SectionHeader
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.data.model.AbilityDetail
import io.github.linxiks.pindex.ui.PokemonGrid
import io.github.linxiks.pindex.ui.preview.SampleData

@Composable
fun AbilityDetailScreen(
    onBack: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
    viewModel: AbilityDetailViewModel = viewModel(factory = AbilityDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AbilityDetailContent(state = state, onBack = onBack, onOpenPokemon = onOpenPokemon)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilityDetailContent(
    state: AbilityUiState,
    onBack: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (state) {
            // Loading is a single local query; a blank frame avoids a spinner flash.
            AbilityUiState.Loading -> Spacer(modifier)
            AbilityUiState.NotFound -> EmptyState(
                title = stringResource(R.string.ability_not_found_title),
                message = stringResource(R.string.ability_not_found_message),
                modifier = modifier,
            )
            AbilityUiState.Error -> EmptyState(
                title = stringResource(R.string.load_error_title),
                message = stringResource(R.string.load_error_message),
                modifier = modifier,
            )
            is AbilityUiState.Content -> AbilityBody(state.detail, onOpenPokemon, modifier)
        }
    }
}

@Composable
private fun AbilityBody(detail: AbilityDetail, onOpenPokemon: (Int) -> Unit, modifier: Modifier) {
    PokemonGrid(
        items = detail.holders,
        onOpenPokemon = onOpenPokemon,
        modifier = modifier,
        header = {
            item(key = "name", span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    LocalizedLabel(
                        text = detail.name,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                    detail.enName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            item(key = "effect", span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    SectionHeader(stringResource(R.string.section_ability_effect))
                    val effect = detail.effect
                    if (effect != null) {
                        LocalizedLabel(effect, MaterialTheme.typography.bodyLarge)
                    } else {
                        Text(stringResource(R.string.flavor_missing), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            item(key = "holdersHeader", span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    SectionHeader(stringResource(R.string.section_ability_holders))
                    if (detail.holders.isEmpty()) {
                        Text(
                            text = stringResource(R.string.ability_holders_none),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun AbilityDetailContentPreview() {
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AbilityDetailContent(AbilityUiState.Content(SampleData.intimidate), onBack = {}, onOpenPokemon = {})
        }
    }
}
