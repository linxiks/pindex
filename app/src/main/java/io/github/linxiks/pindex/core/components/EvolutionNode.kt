package io.github.linxiks.pindex.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.domain.LocalizedText
import io.github.linxiks.pindex.domain.formatNumber

/** One species in an evolution chain; [onClick] null makes it inert (the current species). */
@Composable
fun EvolutionNode(
    name: LocalizedText,
    speciesId: Int,
    isCurrent: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(96.dp)
            .clip(MaterialTheme.shapes.small)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PokemonImage(
            contentDescription = stringResource(R.string.image_description, name.text),
            modifier = Modifier.width(64.dp),
        )
        Text(
            text = formatNumber(speciesId),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val style = MaterialTheme.typography.bodyMedium
        LocalizedLabel(
            text = name,
            style = if (isCurrent) style.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) else style,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewLightDark
@Composable
private fun EvolutionNodePreview() {
    PindexTheme {
        Surface {
            Row {
                EvolutionNode(LocalizedText("皮丘", "zh-Hans"), 172, isCurrent = false, onClick = {})
                EvolutionNode(LocalizedText("皮卡丘", "zh-Hans"), 25, isCurrent = true, onClick = null)
            }
        }
    }
}
