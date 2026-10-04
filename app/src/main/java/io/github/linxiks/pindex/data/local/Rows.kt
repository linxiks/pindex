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
)

data class StatRow(val statId: Int, val baseValue: Int)

data class FormRow(val pokemonId: Int, val identifier: String, val formId: Int?)

data class SearchIdRow(val entityId: Int, val display: String)

data class SearchRow(val entityId: Int, val display: String, val priority: Int)
