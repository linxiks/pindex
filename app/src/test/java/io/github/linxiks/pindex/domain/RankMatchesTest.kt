package io.github.linxiks.pindex.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RankMatchesTest {
    @Test
    fun `prefix hits rank before contains hits even with worse priority`() {
        val ranked = rankMatches(
            prefix = listOf(TermMatch(entityId = 2, priority = 9)),
            contains = listOf(TermMatch(entityId = 1, priority = 0)),
        )
        assertEquals(listOf(2, 1), ranked)
    }

    @Test
    fun `entity in both groups appears once, as a prefix hit`() {
        val ranked = rankMatches(
            prefix = listOf(TermMatch(5, 3)),
            contains = listOf(TermMatch(5, 0), TermMatch(4, 1)),
        )
        assertEquals(listOf(5, 4), ranked)
    }

    @Test
    fun `within a group, priority then entity id`() {
        val ranked = rankMatches(
            prefix = listOf(TermMatch(9, 1), TermMatch(3, 2), TermMatch(7, 1)),
            contains = emptyList(),
        )
        assertEquals(listOf(7, 9, 3), ranked)
    }
}
