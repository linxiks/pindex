package io.github.linxiks.pindex.data

import io.github.linxiks.pindex.data.repository.MetaRepository
import io.github.linxiks.pindex.data.repository.PokemonRepository
import io.github.linxiks.pindex.domain.LocalizedText
import io.github.linxiks.pindex.domain.filterByGeneration
import io.github.linxiks.pindex.testutil.JdbcMetaDao
import io.github.linxiks.pindex.testutil.JdbcPokemonDao
import io.github.linxiks.pindex.testutil.PokedexJdbc
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Runs the real repositories over the Queries constants against data/generated/pokedex.db. */
class PokedexDataTest {
    private val pokemon = PokemonRepository(JdbcPokemonDao())

    @Test
    fun metaAndUserVersionMatchSchemaVersion() = runTest {
        val meta = JdbcMetaDao().all().associate { it.key to it.value }
        assertEquals("1", meta["schema_version"])
        assertEquals("1", meta["data_version"])
        assertEquals(1, PokedexJdbc.userVersion())
        val version = MetaRepository(JdbcMetaDao()).dataVersion()
        assertEquals("1", version.dataVersion)
        assertTrue(version.buildDate.isNotBlank())
    }

    @Test
    fun listHasEveryDefaultPokemonInDexOrder() = runTest {
        val list = pokemon.list()
        assertEquals((1..1025).toList(), list.map { it.speciesId })
        assertTrue(list.all { it.types.isNotEmpty() })
        assertTrue(list.none { it.name.lang == "und" })
    }

    @Test
    fun generationOneFilterKeeps151() = runTest {
        val gen1 = filterByGeneration(pokemon.list(), 1) { it.generationId }
        assertEquals(151, gen1.size)
        assertEquals(151, gen1.last().speciesId)
    }

    @Test
    fun generationsAreNineWithChineseNames() = runTest {
        val generations = pokemon.generations()
        assertEquals((1..9).toList(), generations.map { it.id })
        assertEquals(LocalizedText("第一世代", "zh-Hans"), generations.first().name)
    }

    @Test
    fun pikachuDetail() = runTest {
        val d = checkNotNull(pokemon.detail(25))
        assertEquals(LocalizedText("皮卡丘", "zh-Hans"), d.name)
        assertEquals("Pikachu", d.enName)
        assertEquals("ピカチュウ", d.jaName)
        assertNull(d.formName)
        assertEquals(listOf("electric"), d.types.map { it.identifier })
        assertEquals(listOf(35, 55, 40, 50, 50, 90), d.stats.map { it.value })
        assertEquals(320, d.stats.sumOf { it.value })
        assertTrue(d.stats.all { it.label.lang == "zh-Hans" })
        assertEquals(LocalizedText("鼠宝可梦", "zh-Hans"), d.genus)
        assertEquals(listOf("陆上", "妖精"), d.eggGroups.map { it.text })
        assertEquals("medium", d.growthRateIdentifier)
        assertEquals("第一世代", d.generation?.text)
        assertEquals(4, d.height)
        assertEquals(60, d.weight)
        assertEquals(4, d.genderRate)
        assertEquals(190, d.captureRate)
        assertEquals(112, d.baseExperience)
        assertTrue(d.otherForms.isNotEmpty())
        assertFalse(d.otherForms.any { it.pokemonId == 25 })
    }

    @Test
    fun nonDefaultFormLinksBackToDefault() = runTest {
        val pikachu = checkNotNull(pokemon.detail(25))
        val other = checkNotNull(pokemon.detail(pikachu.otherForms.first().pokemonId))
        assertFalse(other.isDefault)
        assertEquals(25, other.speciesId)
        assertTrue(other.formName != null)
        assertTrue(other.otherForms.any { it.pokemonId == 25 })
    }

    @Test
    fun genusFallsBackToEnglish() = runTest {
        val d = checkNotNull(pokemon.detail(1011))
        assertEquals(LocalizedText("Candy Apple Pokémon", "en"), d.genus)
    }

    @Test
    fun unknownPokemonIsNull() = runTest {
        assertNull(pokemon.detail(99999))
    }
}
