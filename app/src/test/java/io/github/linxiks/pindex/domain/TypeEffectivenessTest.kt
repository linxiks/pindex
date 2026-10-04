package io.github.linxiks.pindex.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TypeEffectivenessTest {
    // Type ids: 1 normal, 5 ground, 8 ghost, 10 fire, 11 water, 12 grass, 13 electric, 15 ice, 16 dragon.
    private val factors = mapOf(
        (10 to 12) to 200,
        (15 to 16) to 200,
        (15 to 5) to 200,
        (8 to 1) to 0,
        (13 to 11) to 200,
        (13 to 5) to 0,
    )

    private fun single(attack: Int, vararg defend: Int) =
        damageTaken(factors, listOf(attack), defend.toList()).getValue(attack)

    @Test
    fun singleTypeWeakness() = assertEquals(200, single(10, 12))

    @Test
    fun dualTypeWeaknessesMultiply() = assertEquals(400, single(15, 16, 5))

    @Test
    fun immunity() = assertEquals(0, single(8, 1))

    @Test
    fun immunityOverridesWeakness() = assertEquals(0, single(13, 11, 5))

    @Test
    fun missingPairIsNeutral() = assertEquals(100, single(1, 12))

    @Test
    fun groupsFollowDisplayOrderWithoutNeutralAndKeepEmptyGroups() {
        val groups = groupDamage(mapOf(13 to 0, 12 to 200, 10 to 200, 1 to 100, 11 to 50))
        assertEquals(
            listOf(400 to emptyList(), 200 to listOf(10, 12), 50 to listOf(11), 25 to emptyList(), 0 to listOf(13)),
            groups,
        )
    }
}
