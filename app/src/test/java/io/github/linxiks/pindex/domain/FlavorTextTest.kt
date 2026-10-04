package io.github.linxiks.pindex.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FlavorTextTest {
    @Test
    fun softHyphenBreakJoinsWord() {
        assertEquals(
            "Powers down supereffective moves.",
            normalizeFlavorText("Powers down super\u00AD\neffective moves.", "en"),
        )
    }

    @Test
    fun chineseDropsBreaks() {
        assertEquals(
            "受到攻击时，用粗糙的皮肤弄伤接触到自己的对手。",
            normalizeFlavorText("受到攻击时，\n用粗糙的皮肤弄伤\n接触到自己的对手。", "zh-Hans"),
        )
    }

    @Test
    fun japaneseUsesIdeographicSpace() {
        assertEquals("攻撃を　受けたとき　自分に", normalizeFlavorText("攻撃を　受けたとき\n自分に", "ja"))
    }

    @Test
    fun englishFormFeedAndNewlineBecomeSpaces() {
        assertEquals("A strong blast hits.", normalizeFlavorText("A strong\u000Cblast\nhits.", "en"))
    }
}
