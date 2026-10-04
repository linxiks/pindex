package io.github.linxiks.pindex.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class EvolutionTextTest {
    private val names = mapOf(("item" to 83) to "雷之石", ("item" to 198) to "王者之证")
    private val lookup = { entity: String, id: Int -> names[entity to id] }

    @Test
    fun levelUp() = assertEquals("Lv.24", describeEvolution("level-up", mapOf("minimum_level" to "24"), lookup))

    @Test
    fun useItem() = assertEquals("使用雷之石", describeEvolution("use-item", mapOf("trigger_item_id" to "83"), lookup))

    @Test
    fun tradeWithHeldItemMergesIntoBase() = assertEquals(
        "携带王者之证通信交换",
        describeEvolution("trade", mapOf("held_item_id" to "198"), lookup),
    )

    @Test
    fun zeroFlagsAreIgnored() = assertEquals(
        "Lv.30，将游戏机倒置",
        describeEvolution(
            "level-up",
            mapOf("minimum_level" to "30", "turn_upside_down" to "1", "needs_overworld_rain" to "0"),
            lookup,
        ),
    )

    @Test
    fun unnamedRequiredFormIsOmitted() = assertEquals(
        "使用雷之石",
        describeEvolution("use-item", mapOf("trigger_item_id" to "83", "required_pokemon_form_id" to "25"), lookup),
    )

    @Test
    fun parsesBuilderJson() = assertEquals(
        mapOf("evolution_trigger_id" to "1", "minimum_level" to "24"),
        parseRawConditions("""{"evolution_trigger_id":"1","minimum_level":"24"}"""),
    )

    @Test
    fun unescapesQuotesAndBackslashes() = assertEquals(
        mapOf("a" to "x\"y", "b" to "c\\d"),
        parseRawConditions("""{"a":"x\"y","b":"c\\d"}"""),
    )

    @Test
    fun nonObjectIsEmpty() = assertEquals(emptyMap<String, String>(), parseRawConditions(""))
}
