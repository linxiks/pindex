package io.github.linxiks.pindex.data.repository

import io.github.linxiks.pindex.data.local.NameRow
import io.github.linxiks.pindex.data.local.PokemonDao
import io.github.linxiks.pindex.data.model.FormLink
import io.github.linxiks.pindex.data.model.Generation
import io.github.linxiks.pindex.data.model.PokemonDetail
import io.github.linxiks.pindex.data.model.PokemonListItem
import io.github.linxiks.pindex.data.model.StatValue
import io.github.linxiks.pindex.data.model.TypeInfo
import io.github.linxiks.pindex.domain.LocalizedText
import io.github.linxiks.pindex.domain.formatNumber
import io.github.linxiks.pindex.domain.resolveLocalized
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PokemonRepository(private val dao: PokemonDao) {
    private val listMutex = Mutex()

    @Volatile
    private var listCache: List<PokemonListItem>? = null

    /** All default pokemon in national dex order; loaded once, then kept in memory. */
    suspend fun list(): List<PokemonListItem> {
        listCache?.let { return it }
        return listMutex.withLock {
            listCache ?: loadList().also { listCache = it }
        }
    }

    private suspend fun loadList(): List<PokemonListItem> {
        val rows = dao.defaultPokemon()
        val typesByPokemon = dao.defaultPokemonTypes().groupBy({ it.pokemonId }, { it.typeId })
        val types = typeInfos()
        val speciesNames = dao.namesOfEntity("species").namesById()
        return rows.map { row ->
            PokemonListItem(
                pokemonId = row.pokemonId,
                speciesId = row.speciesId,
                generationId = row.generationId,
                name = speciesNames[row.speciesId] ?: LocalizedText(formatNumber(row.speciesId), "und"),
                types = typesByPokemon[row.pokemonId].orEmpty().mapNotNull { types[it] },
            )
        }
    }

    suspend fun generations(): List<Generation> {
        val names = dao.namesOfEntity("generation").namesById()
        return dao.generationIds().map { id ->
            Generation(id, names[id] ?: LocalizedText(id.toString(), "und"))
        }
    }

    suspend fun detail(pokemonId: Int): PokemonDetail? {
        val row = dao.detail(pokemonId) ?: return null
        val speciesId = row.speciesId

        val speciesRows = dao.namesOfIds("species", listOf(speciesId))
        val speciesByLang = speciesRows.associate { it.lang to it.name }
        val genusByLang = speciesRows.mapNotNull { r -> r.genus?.let { r.lang to it } }.toMap()

        val types = typeInfos()
        val pokemonTypes = dao.pokemonTypes(pokemonId).mapNotNull { types[it.typeId] }

        val statRows = dao.pokemonStats(pokemonId)
        val statNames = dao.namesOfIds("stat", statRows.map { it.statId }).namesById()
        val stats = statRows.map { s ->
            StatValue(s.statId, statNames[s.statId] ?: LocalizedText(s.statId.toString(), "und"), s.baseValue)
        }

        val eggGroupIds = dao.eggGroupIds(speciesId)
        val eggGroupNames = dao.namesOfIds("egg_group", eggGroupIds).namesById()
        val eggGroups = eggGroupIds.mapNotNull { eggGroupNames[it] }

        val growthRateIdentifier = dao.growthRateIdentifier(row.growthRateId).orEmpty()
        val growthRateEnName = dao.namesOfIds("growth_rate", listOf(row.growthRateId))
            .firstOrNull { it.lang == "en" }?.name

        val generation = dao.namesOfIds("generation", listOf(row.generationId)).namesById()[row.generationId]

        val forms = dao.speciesForms(speciesId)
        val formNames = dao.namesOfIds("pokemon_form", forms.mapNotNull { it.formId }).namesById()
        fun formName(formId: Int?) = formId?.let { formNames[it] }

        val name = resolveLocalized(speciesByLang) ?: LocalizedText(formatNumber(speciesId), "und")
        return PokemonDetail(
            pokemonId = pokemonId,
            speciesId = speciesId,
            isDefault = row.isDefault,
            name = name,
            enName = speciesByLang["en"],
            jaName = speciesByLang["ja"] ?: speciesByLang["ja-Hrkt"],
            formName = if (row.isDefault) null else formName(forms.firstOrNull { it.pokemonId == pokemonId }?.formId),
            genus = resolveLocalized(genusByLang),
            types = pokemonTypes,
            height = row.height,
            weight = row.weight,
            genderRate = row.genderRate,
            captureRate = row.captureRate,
            baseHappiness = row.baseHappiness,
            hatchCounter = row.hatchCounter,
            baseExperience = row.baseExperience,
            eggGroups = eggGroups,
            growthRateIdentifier = growthRateIdentifier,
            growthRateEnName = growthRateEnName,
            generation = generation,
            stats = stats,
            otherForms = forms.filter { it.pokemonId != pokemonId }
                .map { FormLink(it.pokemonId, formName(it.formId), it.identifier) },
        )
    }

    private suspend fun typeInfos(): Map<Int, TypeInfo> {
        val names = dao.namesOfEntity("type").namesById()
        return dao.types().associate { t ->
            t.id to TypeInfo(t.id, t.identifier, names[t.id] ?: LocalizedText(t.identifier, "und"))
        }
    }
}

/** Groups name rows by entity id and resolves each group with the display fallback order. */
private fun List<NameRow>.namesById(): Map<Int, LocalizedText> =
    groupBy { it.entityId }.mapNotNull { (id, rows) ->
        resolveLocalized(rows.associate { it.lang to it.name })?.let { id to it }
    }.toMap()
