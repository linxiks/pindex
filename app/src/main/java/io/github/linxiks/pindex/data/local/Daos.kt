package io.github.linxiks.pindex.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MetaDao {
    @Query(Queries.META)
    suspend fun all(): List<MetaEntity>
}

@Dao
interface PokemonDao {
    @Query(Queries.DEFAULT_POKEMON)
    suspend fun defaultPokemon(): List<PokemonListRow>

    @Query(Queries.DEFAULT_POKEMON_TYPES)
    suspend fun defaultPokemonTypes(): List<PokemonTypeRow>

    @Query(Queries.TYPES)
    suspend fun types(): List<TypeRow>

    @Query(Queries.GENERATION_IDS)
    suspend fun generationIds(): List<Int>

    @Query(Queries.NAMES_OF_ENTITY)
    suspend fun namesOfEntity(entity: String): List<NameRow>

    @Query(Queries.NAMES_OF_IDS)
    suspend fun namesOfIds(entity: String, ids: List<Int>): List<NameRow>

    @Query(Queries.POKEMON_DETAIL)
    suspend fun detail(pokemonId: Int): PokemonDetailRow?

    @Query(Queries.POKEMON_TYPES)
    suspend fun pokemonTypes(pokemonId: Int): List<PokemonTypeRow>

    @Query(Queries.POKEMON_STATS)
    suspend fun pokemonStats(pokemonId: Int): List<StatRow>

    @Query(Queries.SPECIES_EGG_GROUP_IDS)
    suspend fun eggGroupIds(speciesId: Int): List<Int>

    @Query(Queries.SPECIES_FORMS)
    suspend fun speciesForms(speciesId: Int): List<FormRow>

    @Query(Queries.GROWTH_RATE_IDENTIFIER)
    suspend fun growthRateIdentifier(id: Int): String?

    @Query(Queries.TYPE_EFFICACY)
    suspend fun typeEfficacy(): List<TypeEfficacyRow>

    @Query(Queries.CHAIN_SPECIES)
    suspend fun chainSpecies(chainId: Int): List<ChainSpeciesRow>

    @Query(Queries.CHAIN_EVOLUTIONS)
    suspend fun chainEvolutions(chainId: Int): List<EvolutionRow>

    @Query(Queries.POKEMON_ABILITIES)
    suspend fun pokemonAbilities(pokemonId: Int): List<PokemonAbilityRow>

    @Query(Queries.ABILITY_IDENTIFIER)
    suspend fun abilityIdentifier(abilityId: Int): String?

    @Query(Queries.ABILITY_FLAVOR)
    suspend fun abilityFlavor(ids: List<Int>): List<FlavorRow>

    @Query(Queries.ABILITY_SPECIES)
    suspend fun abilitySpecies(abilityId: Int): List<Int>

    @Query(Queries.MOVE_DETAIL)
    suspend fun move(moveId: Int): MoveRow?

    @Query(Queries.MOVES_OF_IDS)
    suspend fun moves(ids: List<Int>): List<MoveRow>

    @Query(Queries.MOVE_FLAVOR)
    suspend fun moveFlavor(moveId: Int): List<FlavorRow>

    @Query(Queries.MOVE_SPECIES)
    suspend fun moveSpecies(moveId: Int): List<Int>

    @Query(Queries.POKEMON_MOVES)
    suspend fun pokemonMoves(pokemonId: Int): List<LearnRow>

    @Query(Queries.VERSION_GROUP_VERSIONS)
    suspend fun versionGroupVersions(): List<VersionRow>
}

@Dao
interface SearchDao {
    @Query(Queries.SEARCH_SPECIES_BY_ID)
    suspend fun speciesById(id: Int): List<SearchIdRow>

    @Query(Queries.SEARCH_PREFIX)
    suspend fun prefix(lo: String, hi: String): List<SearchRow>

    @Query(Queries.SEARCH_CONTAINS)
    suspend fun contains(q: String): List<SearchRow>
}
