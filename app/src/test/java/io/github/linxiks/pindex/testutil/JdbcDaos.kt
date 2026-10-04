package io.github.linxiks.pindex.testutil

import io.github.linxiks.pindex.data.local.FormRow
import io.github.linxiks.pindex.data.local.MetaDao
import io.github.linxiks.pindex.data.local.MetaEntity
import io.github.linxiks.pindex.data.local.NameRow
import io.github.linxiks.pindex.data.local.PokemonDao
import io.github.linxiks.pindex.data.local.PokemonDetailRow
import io.github.linxiks.pindex.data.local.PokemonListRow
import io.github.linxiks.pindex.data.local.PokemonTypeRow
import io.github.linxiks.pindex.data.local.Queries
import io.github.linxiks.pindex.data.local.SearchDao
import io.github.linxiks.pindex.data.local.SearchIdRow
import io.github.linxiks.pindex.data.local.SearchRow
import io.github.linxiks.pindex.data.local.StatRow
import io.github.linxiks.pindex.data.local.TypeRow
import java.sql.ResultSet

// DAO implementations that execute the same Queries constants Room uses, against the real database.

class JdbcMetaDao : MetaDao {
    override suspend fun all() = PokedexJdbc.query(Queries.META) {
        MetaEntity(it.getString("key"), it.getString("value"))
    }
}

private fun typeRow(rs: ResultSet) =
    PokemonTypeRow(rs.getInt("pokemonId"), rs.getInt("slot"), rs.getInt("typeId"))

private fun nameRow(rs: ResultSet) =
    NameRow(rs.getInt("entityId"), rs.getString("lang"), rs.getString("name"), rs.getString("genus"))

private fun searchRow(rs: ResultSet) =
    SearchRow(rs.getInt("entityId"), rs.getString("display"), rs.getInt("priority"))

class JdbcPokemonDao : PokemonDao {
    override suspend fun defaultPokemon() = PokedexJdbc.query(Queries.DEFAULT_POKEMON) {
        PokemonListRow(it.getInt("pokemonId"), it.getInt("speciesId"), it.getInt("generationId"))
    }

    override suspend fun defaultPokemonTypes() = PokedexJdbc.query(Queries.DEFAULT_POKEMON_TYPES, map = ::typeRow)

    override suspend fun types() = PokedexJdbc.query(Queries.TYPES) {
        TypeRow(it.getInt("id"), it.getString("identifier"))
    }

    override suspend fun generationIds() = PokedexJdbc.query(Queries.GENERATION_IDS) { it.getInt(1) }

    override suspend fun namesOfEntity(entity: String) =
        PokedexJdbc.query(Queries.NAMES_OF_ENTITY, mapOf("entity" to entity), ::nameRow)

    override suspend fun namesOfIds(entity: String, ids: List<Int>): List<NameRow> =
        // Room binds an empty IN () fine; JDBC needs at least one placeholder.
        if (ids.isEmpty()) emptyList()
        else PokedexJdbc.query(Queries.NAMES_OF_IDS, mapOf("entity" to entity, "ids" to ids), ::nameRow)

    override suspend fun detail(pokemonId: Int) =
        PokedexJdbc.query(Queries.POKEMON_DETAIL, mapOf("pokemonId" to pokemonId)) {
            PokemonDetailRow(
                pokemonId = it.getInt("pokemonId"),
                speciesId = it.getInt("speciesId"),
                height = it.getInt("height"),
                weight = it.getInt("weight"),
                baseExperience = it.getIntOrNull("baseExperience"),
                isDefault = it.getBool("isDefault"),
                generationId = it.getInt("generationId"),
                genderRate = it.getInt("genderRate"),
                captureRate = it.getInt("captureRate"),
                baseHappiness = it.getInt("baseHappiness"),
                hatchCounter = it.getInt("hatchCounter"),
                growthRateId = it.getInt("growthRateId"),
            )
        }.singleOrNull()

    override suspend fun pokemonTypes(pokemonId: Int) =
        PokedexJdbc.query(Queries.POKEMON_TYPES, mapOf("pokemonId" to pokemonId), ::typeRow)

    override suspend fun pokemonStats(pokemonId: Int) =
        PokedexJdbc.query(Queries.POKEMON_STATS, mapOf("pokemonId" to pokemonId)) {
            StatRow(it.getInt("statId"), it.getInt("baseValue"))
        }

    override suspend fun eggGroupIds(speciesId: Int) =
        PokedexJdbc.query(Queries.SPECIES_EGG_GROUP_IDS, mapOf("speciesId" to speciesId)) { it.getInt(1) }

    override suspend fun speciesForms(speciesId: Int) =
        PokedexJdbc.query(Queries.SPECIES_FORMS, mapOf("speciesId" to speciesId)) {
            FormRow(it.getInt("pokemonId"), it.getString("identifier"), it.getIntOrNull("formId"))
        }

    override suspend fun growthRateIdentifier(id: Int) =
        PokedexJdbc.query(Queries.GROWTH_RATE_IDENTIFIER, mapOf("id" to id)) { it.getString(1) }.singleOrNull()
}

class JdbcSearchDao : SearchDao {
    override suspend fun speciesById(id: Int) =
        PokedexJdbc.query(Queries.SEARCH_SPECIES_BY_ID, mapOf("id" to id)) {
            SearchIdRow(it.getInt("entityId"), it.getString("display"))
        }

    override suspend fun speciesPrefix(lo: String, hi: String) =
        PokedexJdbc.query(Queries.SEARCH_SPECIES_PREFIX, mapOf("lo" to lo, "hi" to hi), ::searchRow)

    override suspend fun speciesContains(q: String) =
        PokedexJdbc.query(Queries.SEARCH_SPECIES_CONTAINS, mapOf("q" to q), ::searchRow)
}
