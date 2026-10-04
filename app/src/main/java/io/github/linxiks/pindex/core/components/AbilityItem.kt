package io.github.linxiks.pindex.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.domain.LocalizedText

/** One ability row; a hidden ability is labelled in text, not only by color. */
@Composable
fun AbilityItem(
    name: LocalizedText,
    effect: LocalizedText?,
    isHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            if (isHidden) {
                Text(
                    text = stringResource(R.string.ability_hidden),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            LocalizedLabel(name, MaterialTheme.typography.titleSmall)
            if (effect != null) {
                LocalizedLabel(
                    text = effect,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

@PreviewLightDark
@Composable
private fun AbilityItemPreview() {
    PindexTheme {
        Surface {
            AbilityItem(
                name = LocalizedText("避雷针", "zh-Hans"),
                effect = LocalizedText("将电属性的招式吸引到自己身上，不会受到伤害，而是会提高特攻。", "zh-Hans"),
                isHidden = true,
                onClick = {},
                modifier = Modifier.padding(horizontal = Spacing.l),
            )
        }
    }
}
