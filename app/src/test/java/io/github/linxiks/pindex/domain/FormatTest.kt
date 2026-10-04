package io.github.linxiks.pindex.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun `gender rate in eighths`() {
        assertEquals("无性别", formatGender(-1))
        assertEquals("♂ 100%", formatGender(0))
        assertEquals("♂ 87.5% / ♀ 12.5%", formatGender(1))
        assertEquals("♂ 50% / ♀ 50%", formatGender(4))
        assertEquals("♀ 100%", formatGender(8))
    }

    @Test
    fun `height and weight convert from decimetres and hectograms`() {
        assertEquals("0.4 m", formatHeight(4))
        assertEquals("6.0 kg", formatWeight(60))
    }

    @Test
    fun `roman numerals`() {
        assertEquals("Ⅰ", toRoman(1))
        assertEquals("Ⅸ", toRoman(9))
        assertEquals("ⅩⅢ", toRoman(13))
    }
}
