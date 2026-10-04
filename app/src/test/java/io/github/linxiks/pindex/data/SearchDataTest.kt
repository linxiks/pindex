package io.github.linxiks.pindex.data

import io.github.linxiks.pindex.data.model.SearchHits
import io.github.linxiks.pindex.data.repository.SearchRepository
import io.github.linxiks.pindex.domain.normalizeQuery
import io.github.linxiks.pindex.testutil.JdbcSearchDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** D2-15, D3-13: user input → normalizeQuery → search against the real search_index. */
class SearchDataTest {
    private val search = SearchRepository(JdbcSearchDao())
    private val none = SearchHits(emptyList(), emptyList(), emptyList())

    private suspend fun hits(input: String) = search.search(normalizeQuery(input))

    @Test
    fun pikachuQueriesRankPikachuFirst() = runTest {
        for (input in listOf("皮卡丘", "皮卡", "Pikachu", "25", "025", "#025")) {
            assertEquals("query $input", 25, hits(input).species.firstOrNull())
        }
    }

    @Test
    fun containsMatchFindsEveryTermWithTheCharacter() = runTest {
        assertEquals(setOf(25, 26, 172, 769, 778), hits("丘").species.toSet())
    }

    @Test
    fun wildcardsAreLiteral() = runTest {
        assertEquals(none, hits("%"))
        assertEquals(none, hits("_"))
    }

    @Test
    fun outOfRangeNumberFindsNothing() = runTest {
        assertEquals(none, hits("9999"))
    }

    @Test
    fun electricHitsEveryCategoryPrefixFirst() = runTest {
        val h = hits("电")
        assertEquals(26, h.species.size)
        assertEquals(31, h.moves.size)
        assertEquals(12, h.abilities.size)
        assertEquals(listOf(125, 171, 181, 239, 313, 466, 587, 595, 596, 796, 848, 939, 940), h.species.take(13))
        assertEquals(
            listOf(84, 86, 98, 192, 209, 351, 393, 486, 527, 604, 729, 754, 804, 892, 905),
            h.moves.take(15),
        )
        assertEquals(listOf(78, 206, 226, 262, 280), h.abilities.take(5))
    }

    @Test
    fun numberMatchesOnlyPokemon() = runTest {
        assertEquals(SearchHits(listOf(25), emptyList(), emptyList()), hits("25"))
    }

    @Test
    fun itemsAreNotSearched() = runTest {
        assertEquals(none, hits("精灵球"))
    }
}
