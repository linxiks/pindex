package io.github.linxiks.pindex.core.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.domain.LocalizedText

/** Suffix marking a fallback language, or null when [text] is in zh-Hans (or has no known source language). */
@Composable
fun fallbackSuffix(text: LocalizedText): String? = when (text.lang) {
    "zh-Hant" -> stringResource(R.string.fallback_zh_hant)
    "en" -> stringResource(R.string.fallback_en)
    else -> null
}

/** [text] followed by a small "(繁体中文)" / "(英文原文)" marker when it is a fallback. */
@Composable
fun localizedAnnotated(text: LocalizedText): AnnotatedString {
    val suffix = fallbackSuffix(text) ?: return AnnotatedString(text.text)
    val suffixStyle = MaterialTheme.typography.labelMedium
    val suffixColor = MaterialTheme.colorScheme.onSurfaceVariant
    return buildAnnotatedString {
        append(text.text)
        withStyle(SpanStyle(fontSize = suffixStyle.fontSize, color = suffixColor)) { append(suffix) }
    }
}

/** Localized text with its fallback marker; see [localizedAnnotated]. */
@Composable
fun LocalizedLabel(
    text: LocalizedText,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    Text(
        text = localizedAnnotated(text),
        style = style,
        color = color,
        textAlign = textAlign,
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun LocalizedLabelPreview() {
    PindexTheme {
        Surface {
            LocalizedLabel(
                text = LocalizedText("Candy Apple Pokémon", "en"),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
