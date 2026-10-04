package io.github.linxiks.pindex.testutil

import io.github.linxiks.pindex.data.local.ChainSpeciesRow
import io.github.linxiks.pindex.data.local.EvolutionRow
import io.github.linxiks.pindex.data.local.FormRow
import io.github.linxiks.pindex.data.local.NameRow
import io.github.linxiks.pindex.data.local.PokemonDao
import io.github.linxiks.pindex.data.local.PokemonDetailRow
import io.github.linxiks.pindex.data.local.PokemonListRow
import io.github.linxiks.pindex.data.local.PokemonTypeRow
import io.github.linxiks.pindex.data.local.SearchDao
import io.github.linxiks.pindex.data.local.SearchIdRow
import io.github.linxiks.pindex.data.local.SearchRow
import io.github.linxiks.pindex.data.local.StatRow
import io.github.linxiks.pindex.data.local.TypeRow
import io.github.linxiks.pindex.data.local.TypeEfficacyRow

/** Three default pokemon: 1 and 25 in generation 1, 152 in generation 2. */
class FakePokemonDao : PokemonDao {
    override suspend fun defaultPokemon() = listOf(
        PokemonListRow(1, 1, 1),
        PokemonListRow(25, 25, 1),
        PokemonListRow(152, 152, 2),
    )

    override suspend fun defaultPokemonTypes() = listOf(
        PokemonTypeRow(1, 1, 12),
        PokemonTypeRow(25, 1, 13),
        PokemonTypeRow(152, 1, 12),
    )

    override suspend fun types() = listOf(TypeRow(12, "grass"), TypeRow(13, "electric"))

    override suspend fun generationIds() = listOf(1, 2)

    override suspend fun namesOfEntity(entity: String): List<NameRow> = when (entity) {
        "species" -> listOf(
            NameRow(1, "zh-Hans", "妙蛙种子", null),
            NameRow(25, "zh-Hans", "皮卡丘", null),
            NameRow(152, "zh-Hans", "菊草叶", null),
        )
        "type" -> listOf(NameRow(12, "zh-Hans", "草", null), NameRow(13, "zh-Hans", "电", null))
        "generation" -> listOf(NameRow(1, "zh-Hans", "第一世代", null), NameRow(2, "zh-Hans", "第二世代", null))
        else -> emptyList()
    }

    override suspend fun namesOfIds(entity: String, ids: List<Int>) =
        namesOfEntity(entity).filter { it.entityId in ids }

    override suspend fun detail(pokemonId: Int): PokemonDetailRow? = null
    override suspend fun pokemonTypes(pokemonId: Int) = emptyList<PokemonTypeRow>()
    override suspend fun pokemonStats(pokemonId: Int) = emptyList<StatRow>()
    override suspend fun eggGroupIds(speciesId: Int) = emptyList<Int>()
    override suspend fun speciesForms(speciesId: Int) = emptyList<FormRow>()
    override suspend fun growthRateIdentifier(id: Int): String? = null
    override suspend fun typeEfficacy() = emptyList<TypeEfficacyRow>()
    override suspend fun chainSpecies(chainId: Int) = emptyList<ChainSpeciesRow>()
    override suspend fun chainEvolutions(chainId: Int) = emptyList<EvolutionRow>()
}

/** Matches "皮卡丘" by prefix (皮, 皮卡, 皮卡丘) and by contains (卡, 丘); nothing else. */
class FakeSearchDao : SearchDao {
    private val term = "皮卡丘"

    override suspend fun speciesById(id: Int) =
        if (id == 25) listOf(SearchIdRow(25, "皮卡丘")) else emptyList()

    override suspend fun speciesPrefix(lo: String, hi: String) =
        if (term >= lo && term < hi) listOf(SearchRow(25, "皮卡丘", 0)) else emptyList()

    override suspend fun speciesContains(q: String) =
        if (term.contains(q)) listOf(SearchRow(25, "皮卡丘", 0)) else emptyList()
}
