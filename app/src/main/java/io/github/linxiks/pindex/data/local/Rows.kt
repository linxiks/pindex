package io.github.linxiks.pindex.data.local

// Projections for Queries; property names match the SQL column aliases.

data class PokemonListRow(val pokemonId: Int, val speciesId: Int, val generationId: Int)

data class PokemonTypeRow(val pokemonId: Int, val slot: Int, val typeId: Int)

data class TypeRow(val id: Int, val identifier: String)

data class NameRow(val entityId: Int, val lang: String, val name: String, val genus: String?)

data class PokemonDetailRow(
    val pokemonId: Int,
    val speciesId: Int,
    val height: Int,
    val weight: Int,
    val baseExperience: Int?,
    val isDefault: Boolean,
    val generationId: Int,
    val genderRate: Int,
    val captureRate: Int,
    val baseHappiness: Int,
    val hatchCounter: Int,
    val growthRateId: Int,
    val evolutionChainId: Int,
)

data class StatRow(val statId: Int, val baseValue: Int)

data class FormRow(val pokemonId: Int, val identifier: String, val formId: Int?, val isDefault: Boolean)

data class SearchIdRow(val entityId: Int, val display: String)

data class SearchRow(val entity: String, val entityId: Int, val display: String, val priority: Int)

data class TypeEfficacyRow(val attackTypeId: Int, val defendTypeId: Int, val factor: Int)

data class ChainSpeciesRow(val speciesId: Int, val evolvesFromSpeciesId: Int?, val pokemonId: Int)

data class EvolutionRow(
    val id: Int,
    val evolvedSpeciesId: Int,
    val isDefault: Boolean,
    val evolvedFormId: Int?,
    val triggerIdentifier: String,
    val rawConditions: String,
)

data class PokemonAbilityRow(val slot: Int, val abilityId: Int, val isHidden: Boolean)

data class FlavorRow(val entityId: Int, val lang: String, val text: String, val sortOrder: Int)

data class MoveRow(val id: Int, val typeId: Int, val damageClassId: Int, val power: Int?, val accuracy: Int?, val pp: Int)

data class LearnRow(
    val versionGroupId: Int,
    val moveId: Int,
    val methodIdentifier: String,
    val level: Int,
    val sortOrder: Int?,
    val typeId: Int,
    val damageClassId: Int,
    val power: Int?,
)

data class VersionRow(val versionGroupId: Int, val identifier: String, val sortOrder: Int, val versionId: Int)
