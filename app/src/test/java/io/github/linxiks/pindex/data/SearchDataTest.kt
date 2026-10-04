package io.github.linxiks.pindex.data

import io.github.linxiks.pindex.data.repository.SearchRepository
import io.github.linxiks.pindex.domain.normalizeQuery
import io.github.linxiks.pindex.testutil.JdbcSearchDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** D2-15: user input → normalizeQuery → searchSpecies against the real search_index. */
class SearchDataTest {
    private val search = SearchRepository(JdbcSearchDao())

    private suspend fun ids(input: String) = search.searchSpecies(normalizeQuery(input))

    @Test
    fun pikachuQueriesRankPikachuFirst() = runTest {
        for (input in listOf("皮卡丘", "皮卡", "Pikachu", "25", "025", "#025")) {
            assertEquals("query $input", 25, ids(input).firstOrNull())
        }
    }

    @Test
    fun containsMatchFindsEveryTermWithTheCharacter() = runTest {
        assertEquals(setOf(25, 26, 172, 769, 778), ids("丘").toSet())
    }

    @Test
    fun wildcardsAreLiteral() = runTest {
        assertTrue(ids("%").isEmpty())
        assertTrue(ids("_").isEmpty())
    }

    @Test
    fun outOfRangeNumberFindsNothing() = runTest {
        assertTrue(ids("9999").isEmpty())
    }
}
