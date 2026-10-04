package io.github.linxiks.pindex.data.model

import io.github.linxiks.pindex.domain.LocalizedText

data class TypeInfo(val id: Int, val identifier: String, val name: LocalizedText)

data class PokemonListItem(
    val pokemonId: Int,
    val speciesId: Int,
    val generationId: Int,
    val name: LocalizedText,
    val types: List<TypeInfo>,
)

data class Generation(val id: Int, val name: LocalizedText)

data class StatValue(val statId: Int, val label: LocalizedText, val value: Int)

data class FormLink(val pokemonId: Int, val name: LocalizedText?, val identifier: String)

data class PokemonDetail(
    val pokemonId: Int,
    val speciesId: Int,
    val isDefault: Boolean,
    val name: LocalizedText,
    val enName: String?,
    val jaName: String?,
    /** Form name of a non-default pokemon; null for the default one. */
    val formName: LocalizedText?,
    val genus: LocalizedText?,
    val types: List<TypeInfo>,
    /** Decimetres. */
    val height: Int,
    /** Hectograms. */
    val weight: Int,
    val genderRate: Int,
    val captureRate: Int,
    val baseHappiness: Int,
    val hatchCounter: Int,
    val baseExperience: Int?,
    val eggGroups: List<LocalizedText>,
    val growthRateIdentifier: String,
    val growthRateEnName: String?,
    val generation: LocalizedText?,
    val stats: List<StatValue>,
    /** Other pokemon of the same species, excluding this one. */
    val otherForms: List<FormLink>,
)

data class DataVersion(val dataVersion: String, val buildDate: String)
