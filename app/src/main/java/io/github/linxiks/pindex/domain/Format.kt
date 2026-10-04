package io.github.linxiks.pindex.domain

import java.util.Locale

fun formatNumber(speciesId: Int): String = "#%04d".format(Locale.ROOT, speciesId)

/** Height is stored in decimetres. */
fun formatHeight(dm: Int): String = "%.1f m".format(Locale.ROOT, dm / 10.0)

/** Weight is stored in hectograms. */
fun formatWeight(hg: Int): String = "%.1f kg".format(Locale.ROOT, hg / 10.0)

/** gender_rate is the female share in eighths; -1 means genderless. */
fun formatGender(rate: Int): String = when (rate) {
    -1 -> "无性别"
    0 -> "♂ 100%"
    8 -> "♀ 100%"
    else -> "♂ ${pct((8 - rate) * 12.5)} / ♀ ${pct(rate * 12.5)}"
}

private fun pct(value: Double): String =
    if (value % 1.0 == 0.0) "${value.toInt()}%" else "$value%"

private const val ROMAN_SINGLE = "ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫ"

/** Unicode upper-case Roman numerals for 1..39: Ⅰ..Ⅻ as single characters, then Ⅹ repeated plus the ones digit. */
fun toRoman(n: Int): String {
    require(n in 1..39) { "toRoman supports 1..39, got $n" }
    if (n <= 12) return ROMAN_SINGLE[n - 1].toString()
    val ones = n % 10
    return buildString {
        repeat(n / 10) { append('Ⅹ') }
        if (ones > 0) append(ROMAN_SINGLE[ones - 1])
    }
}
