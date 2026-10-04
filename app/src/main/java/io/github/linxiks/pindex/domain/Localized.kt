package io.github.linxiks.pindex.domain

/** A display string plus the language it actually came from (after fallback). */
data class LocalizedText(val text: String, val lang: String)

/** Display fallback order. ja is intentionally absent: it is shown only as the explicit Japanese name. */
val FALLBACK_LANGS = listOf("zh-Hans", "zh-Hant", "en")

/** First non-blank value in [FALLBACK_LANGS] order; null when none of them exist. */
fun resolveLocalized(byLang: Map<String, String>): LocalizedText? {
    for (lang in FALLBACK_LANGS) {
        val text = byLang[lang]
        if (!text.isNullOrBlank()) return LocalizedText(text, lang)
    }
    return null
}

fun LocalizedText.isFallback(): Boolean = lang != "zh-Hans"
