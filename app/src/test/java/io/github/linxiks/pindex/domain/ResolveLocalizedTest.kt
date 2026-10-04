package io.github.linxiks.pindex.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolveLocalizedTest {
    @Test
    fun `zh-Hans wins over every fallback`() {
        val names = mapOf("en" to "Pikachu", "zh-Hant" to "皮卡丘(繁)", "zh-Hans" to "皮卡丘")
        assertEquals(LocalizedText("皮卡丘", "zh-Hans"), resolveLocalized(names))
    }

    @Test
    fun `zh-Hant is used before en`() {
        val names = mapOf("en" to "Pikachu", "zh-Hant" to "皮卡丘")
        assertEquals(LocalizedText("皮卡丘", "zh-Hant"), resolveLocalized(names))
    }

    @Test
    fun `en is the last fallback`() {
        assertEquals(LocalizedText("Pikachu", "en"), resolveLocalized(mapOf("en" to "Pikachu")))
    }

    @Test
    fun `ja alone does not resolve`() {
        assertNull(resolveLocalized(mapOf("ja" to "ピカチュウ")))
    }
}
