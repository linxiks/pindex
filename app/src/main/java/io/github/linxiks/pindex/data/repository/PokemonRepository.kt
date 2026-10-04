package io.github.linxiks.pindex.data.repository

import io.github.linxiks.pindex.data.local.ChainSpeciesRow
import io.github.linxiks.pindex.data.local.EvolutionRow
import io.github.linxiks.pindex.data.local.FlavorRow
import io.github.linxiks.pindex.data.local.NameRow
import io.github.linxiks.pindex.data.local.PokemonDao
import io.github.linxiks.pindex.data.model.AbilityDetail
import io.github.linxiks.pindex.data.model.AbilitySlot
import io.github.linxiks.pindex.data.model.AbilitySummary
import io.github.linxiks.pindex.data.model.DamageGroup
import io.github.linxiks.pindex.data.model.EvolutionNode
import io.github.linxiks.pindex.data.model.FormLink
import io.github.linxiks.pindex.data.model.Generation
import io.github.linxiks.pindex.data.model.LearnedMove
import io.github.linxiks.pindex.data.model.Learnset
import io.github.linxiks.pindex.data.model.MoveDetail
import io.github.linxiks.pindex.data.model.MoveSummary
import io.github.linxiks.pindex.data.model.PokemonDetail
import io.github.linxiks.pindex.data.model.PokemonListItem
import io.github.linxiks.pindex.data.model.StatValue
import io.github.linxiks.pindex.data.model.TypeInfo
import io.github.linxiks.pindex.data.model.VersionGroupOption
import io.github.linxiks.pindex.domain.EVOLUTION_NAME_REFS
import io.github.linxiks.pindex.domain.FALLBACK_LANGS
import io.github.linxiks.pindex.domain.LocalizedText
import io.github.linxiks.pindex.domain.damageTaken
import io.github.linxiks.pindex.domain.describeEvolution
import io.github.linxiks.pindex.domain.formatNumber
import io.github.linxiks.pindex.domain.groupDamage
import io.github.linxiks.pindex.domain.moveMethodGroup
import io.github.linxiks.pindex.domain.normalizeFlavorText
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

        val abilityRows = dao.pokemonAbilities(pokemonId)
        val abilityIds = abilityRows.map { it.abilityId }
        val abilityNames = dao.namesOfIds("ability", abilityIds).namesById()
        val abilityEffects = dao.abilityFlavor(abilityIds).latestByEntity()
        val abilities = abilityRows.map { a ->
            AbilitySlot(
                abilityId = a.abilityId,
                name = abilityNames[a.abilityId] ?: LocalizedText(a.abilityId.toString(), "und"),
                effect = abilityEffects[a.abilityId],
                isHidden = a.isHidden,
            )
        }

        val name = resolveLocalized(speciesByLang) ?: LocalizedText(formatNumber(speciesId), "und")
        return PokemonDetail(
            pokemonId = pokemonId,
            speciesId = speciesId,
            isDefault = row.isDefault,
            name = name,
            enName = speciesByLang["en"],
            jaName = speciesByLang["ja"] ?: speciesByLang["ja-Hrkt"],
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
            abilities = abilities,
            learnset = learnset(pokemonId),
            forms = forms.map { FormLink(it.pokemonId, formName(it.formId), it.identifier, it.isDefault) },
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

    /**
     * Every way [pokemonId] learns moves, across all version groups. Default group: newest one
     * with level-up moves (the newest overall can be a special-method-only group such as champions).
     */
    suspend fun learnset(pokemonId: Int): Learnset {
        val rows = dao.pokemonMoves(pokemonId)
        if (rows.isEmpty()) return Learnset(emptyList(), null, emptyList())

        val moveNames = dao.namesOfIds("move", rows.map { it.moveId }.distinct()).namesById()
        val types = typeInfos()
        val damageClasses = dao.namesOfEntity("move_damage_class").namesById()

        val usedGroups = rows.mapTo(HashSet()) { it.versionGroupId }
        val versionRows = dao.versionGroupVersions().filter { it.versionGroupId in usedGroups }
        val versionNames = dao.namesOfIds("version", versionRows.map { it.versionId }).namesById()
        val versionGroups = versionRows.groupBy { it.versionGroupId }.values
            .sortedByDescending { it.first().sortOrder }
            .map { g ->
                VersionGroupOption(g.first().versionGroupId, g.first().identifier, g.mapNotNull { versionNames[it.versionId] })
            }

        val levelUpGroups = rows.filter { it.methodIdentifier == "level-up" }.mapTo(HashSet()) { it.versionGroupId }
        val defaultGroup = versionGroups.firstOrNull { it.id in levelUpGroups } ?: versionGroups.firstOrNull()

        val moves = rows.map { r ->
            val learned = LearnedMove(
                moveId = r.moveId,
                versionGroupId = r.versionGroupId,
                group = moveMethodGroup(r.methodIdentifier),
                level = r.level,
                name = moveNames[r.moveId] ?: LocalizedText(r.moveId.toString(), "und"),
                type = types.getValue(r.typeId),
                damageClass = damageClasses[r.damageClassId] ?: LocalizedText(r.damageClassId.toString(), "und"),
                power = r.power,
            )
            learned to (r.sortOrder ?: Int.MAX_VALUE)
        }
            // Other merges several methods: one move can appear twice at the same level in a group.
            .distinctBy { (m, _) -> listOf(m.versionGroupId, m.group.ordinal, m.moveId, m.level) }
            .sortedWith(
                compareBy<Pair<LearnedMove, Int>>({ it.first.group.ordinal }, { it.first.level }, { it.second }, { it.first.moveId }),
            )
            .map { it.first }
        return Learnset(versionGroups, defaultGroup?.id, moves)
    }

    suspend fun ability(abilityId: Int): AbilityDetail? {
        val identifier = dao.abilityIdentifier(abilityId) ?: return null
        val nameRows = dao.namesOfIds("ability", listOf(abilityId))
        val byLang = nameRows.associate { it.lang to it.name }
        val name = resolveLocalized(byLang) ?: LocalizedText(identifier, "und")
        return AbilityDetail(
            abilityId = abilityId,
            name = name,
            enName = byLang["en"].takeIf { name.lang != "en" },
            effect = dao.abilityFlavor(listOf(abilityId)).latestByEntity()[abilityId],
            holders = bySpecies(dao.abilitySpecies(abilityId)),
        )
    }

    suspend fun move(moveId: Int): MoveDetail? {
        val row = dao.move(moveId) ?: return null
        val byLang = dao.namesOfIds("move", listOf(moveId)).associate { it.lang to it.name }
        val name = resolveLocalized(byLang) ?: LocalizedText(moveId.toString(), "und")
        val damageClass = dao.namesOfIds("move_damage_class", listOf(row.damageClassId)).namesById()[row.damageClassId]
            ?: LocalizedText(row.damageClassId.toString(), "und")
        return MoveDetail(
            moveId = moveId,
            name = name,
            enName = byLang["en"].takeIf { name.lang != "en" },
            type = typeInfos().getValue(row.typeId),
            damageClass = damageClass,
            power = row.power,
            accuracy = row.accuracy,
            pp = row.pp,
            description = dao.moveFlavor(moveId).latestByEntity()[moveId],
            learners = bySpecies(dao.moveSpecies(moveId)),
        )
    }

    /** Search rows for [ids], in input order; ids missing from the database are skipped. */
    suspend fun moveSummaries(ids: List<Int>): List<MoveSummary> {
        if (ids.isEmpty()) return emptyList()
        val rows = dao.moves(ids).associateBy { it.id }
        val names = dao.namesOfIds("move", ids).namesById()
        val types = typeInfos()
        val damageClasses = dao.namesOfEntity("move_damage_class").namesById()
        return ids.mapNotNull { id ->
            val row = rows[id] ?: return@mapNotNull null
            MoveSummary(
                moveId = id,
                name = names[id] ?: LocalizedText(id.toString(), "und"),
                type = types.getValue(row.typeId),
                damageClass = damageClasses[row.damageClassId] ?: LocalizedText(row.damageClassId.toString(), "und"),
                power = row.power,
            )
        }
    }

    /** Search rows for [ids], in input order. */
    suspend fun abilitySummaries(ids: List<Int>): List<AbilitySummary> {
        if (ids.isEmpty()) return emptyList()
        val names = dao.namesOfIds("ability", ids).namesById()
        val effects = dao.abilityFlavor(ids).latestByEntity()
        return ids.map { id -> AbilitySummary(id, names[id] ?: LocalizedText(id.toString(), "und"), effects[id]) }
    }

    /** Default pokemon of each species from the list cache, in [speciesIds] order. */
    private suspend fun bySpecies(speciesIds: List<Int>): List<PokemonListItem> {
        val bySpecies = list().associateBy { it.speciesId }
        return speciesIds.mapNotNull { bySpecies[it] }
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

/** Per entity: newest flavor text in the first [FALLBACK_LANGS] language that has any, normalized. */
private fun List<FlavorRow>.latestByEntity(): Map<Int, LocalizedText> =
    groupBy { it.entityId }.mapNotNull { (id, rows) ->
        FALLBACK_LANGS.firstNotNullOfOrNull { lang -> rows.filter { it.lang == lang }.maxByOrNull { it.sortOrder } }
            ?.let { id to LocalizedText(normalizeFlavorText(it.text, it.lang), it.lang) }
    }.toMap()
