package io.github.linxiks.pindex.domain

/** Damage-taken groups shown on the detail page, in display order; 100 (neutral) is not shown. */
val DAMAGE_GROUPS = listOf(400, 200, 50, 25, 0)

/**
 * Percent of damage each attacking type deals to a pokemon of [defendTypeIds].
 * [factors] maps (attack, defend) to type_efficacy.factor (0/50/100/200); a missing pair counts as 100.
 * Result values are exact: 0, 25, 50, 100, 200 or 400.
 */
fun damageTaken(factors: Map<Pair<Int, Int>, Int>, attackTypeIds: List<Int>, defendTypeIds: List<Int>): Map<Int, Int> =
    attackTypeIds.associateWith { a -> defendTypeIds.fold(100) { acc, d -> acc * (factors[a to d] ?: 100) / 100 } }

/** One entry per [DAMAGE_GROUPS] value, attack type ids ascending; groups may be empty. */
fun groupDamage(byAttack: Map<Int, Int>): List<Pair<Int, List<Int>>> =
    DAMAGE_GROUPS.map { p -> p to byAttack.filterValues { it == p }.keys.sorted() }
