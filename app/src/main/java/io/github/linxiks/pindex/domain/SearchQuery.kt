package io.github.linxiks.pindex.domain

import java.text.Normalizer
import java.util.Locale

sealed interface SearchQuery {
    data object Blank : SearchQuery
    data class Number(val id: Int) : SearchQuery
    data class Text(val term: String) : SearchQuery
}

/** Must match tools/data-builder/builder/search.py normalize_term: NFKC, lower case, drop whitespace and '#'. */
fun normalizeTerm(input: String): String =
    Normalizer.normalize(input, Normalizer.Form.NFKC)
        .lowercase(Locale.ROOT)
        .filterNot { it.isWhitespace() || it == '#' }

private val NumberPattern = Regex("^0*(\\d{1,4})$")

fun normalizeQuery(input: String): SearchQuery {
    val term = normalizeTerm(input)
    if (term.isEmpty()) return SearchQuery.Blank
    val number = NumberPattern.matchEntire(term)
    return if (number != null) {
        SearchQuery.Number(number.groupValues[1].toInt())
    } else {
        SearchQuery.Text(term)
    }
}
