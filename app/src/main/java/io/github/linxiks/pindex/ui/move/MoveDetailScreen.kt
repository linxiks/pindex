package io.github.linxiks.pindex.ui.move

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.components.EmptyState
import io.github.linxiks.pindex.core.components.InfoGrid
import io.github.linxiks.pindex.core.components.LocalizedLabel
import io.github.linxiks.pindex.core.components.SectionHeader
import io.github.linxiks.pindex.core.components.TypeChip
import io.github.linxiks.pindex.core.components.localizedAnnotated
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.data.model.MoveDetail
import io.github.linxiks.pindex.ui.PokemonGrid
import io.github.linxiks.pindex.ui.preview.SampleData

@Composable
fun MoveDetailScreen(
    onBack: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
    viewModel: MoveDetailViewModel = viewModel(factory = MoveDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MoveDetailContent(state = state, onBack = onBack, onOpenPokemon = onOpenPokemon)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveDetailContent(
    state: MoveUiState,
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
            MoveUiState.Loading -> Spacer(modifier)
            MoveUiState.NotFound -> EmptyState(
                title = stringResource(R.string.move_not_found_title),
                message = stringResource(R.string.move_not_found_message),
                modifier = modifier,
            )
            MoveUiState.Error -> EmptyState(
                title = stringResource(R.string.load_error_title),
                message = stringResource(R.string.load_error_message),
                modifier = modifier,
            )
            is MoveUiState.Content -> MoveBody(state.detail, onOpenPokemon, modifier)
        }
    }
}

@Composable
private fun MoveBody(detail: MoveDetail, onOpenPokemon: (Int) -> Unit, modifier: Modifier) {
    val missing = stringResource(R.string.value_missing)
    val info = listOf(
        stringResource(R.string.move_damage_class) to localizedAnnotated(detail.damageClass),
        stringResource(R.string.move_power) to AnnotatedString(detail.power?.toString() ?: missing),
        stringResource(R.string.move_accuracy) to AnnotatedString(detail.accuracy?.toString() ?: missing),
        stringResource(R.string.move_pp) to AnnotatedString(detail.pp.toString()),
    )
    PokemonGrid(
        items = detail.learners,
        onOpenPokemon = onOpenPokemon,
        modifier = modifier,
        header = {
            item(key = "name", span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
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
                    TypeChip(name = detail.type.name.text, identifier = detail.type.identifier)
                }
            }
            item(key = "info", span = { GridItemSpan(maxLineSpan) }) {
                InfoGrid(info, Modifier.padding(top = Spacing.l))
            }
            item(key = "description", span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    SectionHeader(stringResource(R.string.section_move_description))
                    val description = detail.description
                    if (description != null) {
                        LocalizedLabel(description, MaterialTheme.typography.bodyLarge)
                    } else {
                        Text(stringResource(R.string.flavor_missing), style = MaterialTheme.typography.bodyLarge)
                    }
                    SectionHeader(stringResource(R.string.section_move_learners))
                    if (detail.learners.isEmpty()) {
                        Text(
                            text = stringResource(R.string.move_learners_none),
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
private fun MoveDetailContentPreview() {
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MoveDetailContent(MoveUiState.Content(SampleData.thunderbolt), onBack = {}, onOpenPokemon = {})
        }
    }
}
