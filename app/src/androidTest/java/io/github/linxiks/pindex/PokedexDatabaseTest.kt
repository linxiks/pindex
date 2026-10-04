package io.github.linxiks.pindex

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Opening the packaged asset through Room proves the entities pass Room's schema validation. */
@RunWith(AndroidJUnit4::class)
class PokedexDatabaseTest {
    @Test
    fun roomOpensPackagedDatabase() = runBlocking {
        val container = AppContainer(ApplicationProvider.getApplicationContext())
        val meta = container.database.metaDao().all().associate { it.key to it.value }
        assertEquals("1", meta["data_version"])
        assertEquals(1025, container.database.pokemonDao().defaultPokemon().size)
    }
}
