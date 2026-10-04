package io.github.linxiks.pindex.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.domain.LocalizedText

/** One learnable move: optional leading label (level), name, type, damage class, power. */
@Composable
fun MoveItem(
    leading: String?,
    name: LocalizedText,
    type: TypeUi,
    damageClass: String,
    power: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        if (leading != null) {
            Text(
                text = leading,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(48.dp),
            )
        }
        LocalizedLabel(name, MaterialTheme.typography.bodyLarge, Modifier.weight(1f))
        TypeChip(type.name, type.identifier)
        Text(
            text = damageClass,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = power?.toString() ?: stringResource(R.string.value_missing),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun MoveItemPreview() {
    PindexTheme {
        Surface {
            MoveItem(
                leading = "Lv.36",
                name = LocalizedText("十万伏特", "zh-Hans"),
                type = TypeUi("电", "electric"),
                damageClass = "特殊",
                power = 90,
                onClick = {},
                modifier = Modifier.padding(horizontal = Spacing.l),
            )
        }
    }
}
