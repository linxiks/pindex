package io.github.linxiks.pindex.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizeQueryTest {
    @Test
    fun `number forms with leading zeros, hash and full width digits are national dex numbers`() {
        listOf("25", "025", "#025", "#0025", " ０２５ ").forEach {
            assertEquals(it, SearchQuery.Number(25), normalizeQuery(it))
        }
    }

    @Test
    fun `text is lower cased and whitespace is dropped`() {
        assertEquals(SearchQuery.Text("pikachu"), normalizeQuery("Pikachu"))
        assertEquals(SearchQuery.Text("皮卡丘"), normalizeQuery("皮卡 丘"))
    }

    @Test
    fun `nothing left after normalization is blank`() {
        assertEquals(SearchQuery.Blank, normalizeQuery(""))
        assertEquals(SearchQuery.Blank, normalizeQuery("#"))
    }

    @Test
    fun `five digits are not a dex number`() {
        assertEquals(SearchQuery.Text("12345"), normalizeQuery("12345"))
    }
}
