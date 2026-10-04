package io.github.linxiks.pindex.ui.pokemon

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
import io.github.linxiks.pindex.core.components.DamageMultiplierGroup
import io.github.linxiks.pindex.core.components.EmptyState
import io.github.linxiks.pindex.core.components.InfoGrid
import io.github.linxiks.pindex.core.components.LocalizedLabel
import io.github.linxiks.pindex.core.components.PokemonImage
import io.github.linxiks.pindex.core.components.SectionHeader
import io.github.linxiks.pindex.core.components.StatBar
import io.github.linxiks.pindex.core.components.StatTotalRow
import io.github.linxiks.pindex.core.components.TypeChip
import io.github.linxiks.pindex.core.components.localizedAnnotated
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.data.model.PokemonDetail
import io.github.linxiks.pindex.domain.formatGender
import io.github.linxiks.pindex.domain.formatHeight
import io.github.linxiks.pindex.domain.formatMultiplier
import io.github.linxiks.pindex.domain.formatNumber
import io.github.linxiks.pindex.domain.formatWeight
import io.github.linxiks.pindex.ui.preview.SampleData

@Composable
fun PokemonDetailScreen(
    onBack: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
    viewModel: PokemonDetailViewModel = viewModel(factory = PokemonDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PokemonDetailContent(state = state, onBack = onBack, onOpenPokemon = onOpenPokemon)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailContent(
    state: DetailUiState,
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
            DetailUiState.Loading -> Spacer(modifier)
            DetailUiState.NotFound -> EmptyState(
                title = stringResource(R.string.detail_not_found_title),
                message = stringResource(R.string.detail_not_found_message),
                modifier = modifier,
            )
            DetailUiState.Error -> EmptyState(
                title = stringResource(R.string.load_error_title),
                message = stringResource(R.string.load_error_message),
                modifier = modifier,
            )
            is DetailUiState.Content -> DetailBody(state.detail, onOpenPokemon, modifier)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailBody(detail: PokemonDetail, onOpenPokemon: (Int) -> Unit, modifier: Modifier) {
    val basicInfo = basicInfoItems(detail)
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = Spacing.l, end = Spacing.l, bottom = Spacing.xl),
    ) {
        item(key = "header") { DetailHeader(detail) }
        item(key = "basic") {
            Column {
                SectionHeader(stringResource(R.string.section_basic_info))
                InfoGrid(basicInfo)
            }
        }
        item(key = "stats") {
            Column {
                SectionHeader(stringResource(R.string.section_stats))
                detail.stats.forEach { StatBar(label = it.label.text, value = it.value) }
                StatTotalRow(label = stringResource(R.string.stat_total), value = detail.stats.sumOf { it.value })
            }
        }
        item(key = "typeDefense") {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                SectionHeader(stringResource(R.string.section_type_defense))
                Text(
                    text = stringResource(R.string.type_defense_caption),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                detail.damageTaken.forEach { DamageMultiplierGroup(formatMultiplier(it.percent), it.types) }
            }
        }
        item(key = "evolution") {
            Column {
                SectionHeader(stringResource(R.string.section_evolution))
                EvolutionSection(detail.evolution, detail.speciesId, onOpenPokemon)
            }
        }
        if (detail.otherForms.isNotEmpty()) {
            item(key = "forms") {
                Column {
                    SectionHeader(stringResource(R.string.section_other_forms))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        detail.otherForms.forEach { form ->
                            SuggestionChip(
                                onClick = { onOpenPokemon(form.pokemonId) },
                                label = {
                                    Text(form.name?.let { localizedAnnotated(it) } ?: AnnotatedString(form.identifier))
                                },
                                shape = MaterialTheme.shapes.small,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(detail: PokemonDetail) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = formatNumber(detail.speciesId),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.s))
        PokemonImage(
            contentDescription = stringResource(R.string.image_description, detail.name.text),
            modifier = Modifier.fillMaxWidth(0.5f),
        )
        Spacer(Modifier.height(Spacing.m))
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
        detail.formName?.let {
            LocalizedLabel(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(Spacing.s))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            detail.types.forEach { TypeChip(name = it.name.text, identifier = it.identifier) }
        }
        Spacer(Modifier.height(Spacing.l))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** design.md §24 order, paired two per row. Japanese name is omitted entirely when missing. */
@Composable
private fun basicInfoItems(detail: PokemonDetail): List<Pair<String, AnnotatedString>> {
    val missing = AnnotatedString(stringResource(R.string.value_missing))
    fun plain(text: String) = AnnotatedString(text)
    val eggGroups = if (detail.eggGroups.isEmpty()) {
        missing
    } else {
        AnnotatedString.Builder().apply {
            detail.eggGroups.forEachIndexed { i, group ->
                if (i > 0) append(" / ")
                append(localizedAnnotated(group))
            }
        }.toAnnotatedString()
    }
    return buildList {
        add(stringResource(R.string.info_genus) to (detail.genus?.let { localizedAnnotated(it) } ?: missing))
        detail.jaName?.let { add(stringResource(R.string.info_ja_name) to plain(it)) }
        add(stringResource(R.string.info_height) to plain(formatHeight(detail.height)))
        add(stringResource(R.string.info_weight) to plain(formatWeight(detail.weight)))
        add(stringResource(R.string.info_gender) to plain(formatGender(detail.genderRate)))
        add(stringResource(R.string.info_capture_rate) to plain(detail.captureRate.toString()))
        add(stringResource(R.string.info_egg_groups) to eggGroups)
        add(stringResource(R.string.info_growth_rate) to plain(growthRateLabel(detail)))
        add(stringResource(R.string.info_base_happiness) to plain(detail.baseHappiness.toString()))
        add(
            stringResource(R.string.info_hatch_counter) to
                plain(stringResource(R.string.info_hatch_counter_value, detail.hatchCounter)),
        )
        add(stringResource(R.string.info_base_experience) to (detail.baseExperience?.let { plain(it.toString()) } ?: missing))
        add(stringResource(R.string.info_generation) to (detail.generation?.let { localizedAnnotated(it) } ?: missing))
    }
}

@StringRes
private fun growthRateRes(identifier: String): Int? = when (identifier) {
    "slow" -> R.string.growth_slow
    "medium" -> R.string.growth_medium
    "fast" -> R.string.growth_fast
    "medium-slow" -> R.string.growth_medium_slow
    "slow-then-very-fast" -> R.string.growth_slow_then_very_fast
    "fast-then-very-slow" -> R.string.growth_fast_then_very_slow
    else -> null
}

@Composable
private fun growthRateLabel(detail: PokemonDetail): String =
    growthRateRes(detail.growthRateIdentifier)?.let { stringResource(it) }
        ?: detail.growthRateEnName
        ?: stringResource(R.string.value_missing)

@PreviewLightDark
@Composable
private fun PokemonDetailContentPreview() {
    PindexTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PokemonDetailContent(DetailUiState.Content(SampleData.pikachu), onBack = {}, onOpenPokemon = {})
        }
    }
}
