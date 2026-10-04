package io.github.linxiks.pindex.domain

/** Game line breaks removed per language: soft hyphen + newline joins; zh drops breaks; ja uses U+3000; others a space. */
fun normalizeFlavorText(text: String, lang: String): String {
    val joined = text.replace("\u00AD\n", "")
    val sep = when {
        lang.startsWith("zh") -> ""
        lang.startsWith("ja") -> "\u3000"
        else -> " "
    }
    return joined.replace("\n", sep).replace("\u000C", sep)
}
