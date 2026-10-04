package io.github.linxiks.pindex.data.repository

import io.github.linxiks.pindex.data.local.ChainSpeciesRow
import io.github.linxiks.pindex.data.local.EvolutionRow
import io.github.linxiks.pindex.data.local.NameRow
import io.github.linxiks.pindex.data.local.PokemonDao
import io.github.linxiks.pindex.data.model.DamageGroup
import io.github.linxiks.pindex.data.model.EvolutionNode
import io.github.linxiks.pindex.data.model.FormLink
import io.github.linxiks.pindex.data.model.Generation
import io.github.linxiks.pindex.data.model.PokemonDetail
import io.github.linxiks.pindex.data.model.PokemonListItem
import io.github.linxiks.pindex.data.model.StatValue
import io.github.linxiks.pindex.data.model.TypeInfo
import io.github.linxiks.pindex.domain.EVOLUTION_NAME_REFS
import io.github.linxiks.pindex.domain.LocalizedText
import io.github.linxiks.pindex.domain.damageTaken
import io.github.linxiks.pindex.domain.describeEvolution
import io.github.linxiks.pindex.domain.formatNumber
import io.github.linxiks.pindex.domain.groupDamage
import io.github.linxiks.pindex.domain.parseRawConditions
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
        val typeIds = dao.pokemonTypes(pokemonId).map { it.typeId }
        val pokemonTypes = typeIds.mapNotNull { types[it] }

        val efficacy = dao.typeEfficacy()
        val factors = efficacy.associate { (it.attackTypeId to it.defendTypeId) to it.factor }
        val attackIds = efficacy.map { it.attackTypeId }.distinct().sorted()
        val damage = groupDamage(damageTaken(factors, attackIds, typeIds))
            .map { (percent, ids) -> DamageGroup(percent, ids.mapNotNull { types[it] }) }

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
            damageTaken = damage,
            evolution = evolution(row.evolutionChainId),
            otherForms = forms.filter { it.pokemonId != pokemonId }
                .map { FormLink(it.pokemonId, formName(it.formId), it.identifier) },
        )
    }

    /** Evolution tree of [chainId]; each evolved species shows one condition (see [pickEvolution]). */
    suspend fun evolution(chainId: Int): List<EvolutionNode> {
        val species = dao.chainSpecies(chainId)
        val chosen = dao.chainEvolutions(chainId).groupBy { it.evolvedSpeciesId }
            .mapValues { (_, rows) -> pickEvolution(rows) }
        val conditions = chosen.mapValues { (_, row) -> parseRawConditions(row.rawConditions) }

        val idsByEntity = mutableMapOf<String, MutableSet<Int>>()
        conditions.values.forEach { c ->
            EVOLUTION_NAME_REFS.forEach { (key, entity) ->
                c[key]?.toIntOrNull()?.let { idsByEntity.getOrPut(entity) { mutableSetOf() } += it }
            }
        }
        idsByEntity.getOrPut("species") { mutableSetOf() } += species.map { it.speciesId }
        val names = idsByEntity.mapValues { (entity, ids) -> dao.namesOfIds(entity, ids.toList()).namesById() }
        val lookup = { entity: String, id: Int -> names[entity]?.get(id)?.text }
        val speciesNames = names["species"].orEmpty()

        val children = species.groupBy { it.evolvesFromSpeciesId }
        fun node(s: ChainSpeciesRow): EvolutionNode = EvolutionNode(
            speciesId = s.speciesId,
            pokemonId = s.pokemonId,
            name = speciesNames[s.speciesId] ?: LocalizedText(formatNumber(s.speciesId), "und"),
            condition = chosen[s.speciesId]?.let { describeEvolution(it.triggerIdentifier, conditions.getValue(s.speciesId), lookup) },
            children = children[s.speciesId].orEmpty().map { node(it) },
        )
        return children[null].orEmpty().map { node(it) }
    }

    private suspend fun typeInfos(): Map<Int, TypeInfo> {
        val names = dao.namesOfEntity("type").namesById()
        return dao.types().associate { t ->
            t.id to TypeInfo(t.id, t.identifier, names[t.id] ?: LocalizedText(t.identifier, "und"))
        }
    }
}

/**
 * One row per evolved species: default condition first, then rows not tied to a regional or
 * alternate form, then the earliest row (e.g. Raichu keeps the Thunder Stone row, not Alolan Raichu).
 */
private fun pickEvolution(rows: List<EvolutionRow>): EvolutionRow =
    rows.minWith(
        compareByDescending<EvolutionRow> { it.isDefault }
            .thenByDescending { it.evolvedFormId == null || it.evolvedFormId == it.evolvedSpeciesId }
            .thenBy { it.id },
    )

/** Groups name rows by entity id and resolves each group with the display fallback order. */
private fun List<NameRow>.namesById(): Map<Int, LocalizedText> =
    groupBy { it.entityId }.mapNotNull { (id, rows) ->
        resolveLocalized(rows.associate { it.lang to it.name })?.let { id to it }
    }.toMap()
