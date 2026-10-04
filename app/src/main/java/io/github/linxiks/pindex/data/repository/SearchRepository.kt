package io.github.linxiks.pindex.data.repository

import io.github.linxiks.pindex.data.local.SearchDao
import io.github.linxiks.pindex.data.local.SearchRow
import io.github.linxiks.pindex.data.model.SearchHits
import io.github.linxiks.pindex.domain.SearchQuery
import io.github.linxiks.pindex.domain.TermMatch
import io.github.linxiks.pindex.domain.rankMatches

/** U+10FFFF: upper bound for a primary-key range scan over every term starting with a prefix. */
private const val MAX_CHAR = "\uDBFF\uDFFF"

class SearchRepository(private val dao: SearchDao) {
    /** Species, move and ability ids matching [query]; numbers match species only. */
    suspend fun search(query: SearchQuery): SearchHits = when (query) {
        SearchQuery.Blank -> SearchHits(emptyList(), emptyList(), emptyList())
        is SearchQuery.Number -> SearchHits(dao.speciesById(query.id).map { it.entityId }, emptyList(), emptyList())
        is SearchQuery.Text -> {
            val prefix = dao.prefix(query.term, query.term + MAX_CHAR).groupBy { it.entity }
            val contains = dao.contains(query.term).groupBy { it.entity }
            fun ranked(entity: String) = rankMatches(
                prefix = prefix[entity].orEmpty().map(::toMatch),
                contains = contains[entity].orEmpty().map(::toMatch),
            )
            SearchHits(ranked("species"), ranked("move"), ranked("ability"))
        }
    }

    private fun toMatch(row: SearchRow) = TermMatch(row.entityId, row.priority)
}
