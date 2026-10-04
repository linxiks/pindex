package io.github.linxiks.pindex.data.repository

import io.github.linxiks.pindex.data.local.SearchDao
import io.github.linxiks.pindex.data.local.SearchRow
import io.github.linxiks.pindex.domain.SearchQuery
import io.github.linxiks.pindex.domain.TermMatch
import io.github.linxiks.pindex.domain.rankMatches

/** U+10FFFF: upper bound for a primary-key range scan over every term starting with a prefix. */
private const val MAX_CHAR = "\uDBFF\uDFFF"

class SearchRepository(private val dao: SearchDao) {
    /** Species ids matching [query], best match first. */
    suspend fun searchSpecies(query: SearchQuery): List<Int> = when (query) {
        SearchQuery.Blank -> emptyList()
        is SearchQuery.Number -> dao.speciesById(query.id).map { it.entityId }
        is SearchQuery.Text -> rankMatches(
            prefix = dao.speciesPrefix(query.term, query.term + MAX_CHAR).map(::toMatch),
            contains = dao.speciesContains(query.term).map(::toMatch),
        )
    }

    private fun toMatch(row: SearchRow) = TermMatch(row.entityId, row.priority)
}
