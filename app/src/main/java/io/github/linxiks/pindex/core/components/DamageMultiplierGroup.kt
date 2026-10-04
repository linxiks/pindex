package io.github.linxiks.pindex.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.data.model.TypeInfo
import io.github.linxiks.pindex.domain.LocalizedText

/** One damage-taken row: multiplier label, then the attacking types, or "—" when none. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DamageMultiplierGroup(label: String, types: List<TypeInfo>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.widthIn(min = 48.dp))
        if (types.isEmpty()) {
            Text(stringResource(R.string.value_missing), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                types.forEach { TypeChip(name = it.name.text, identifier = it.identifier) }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DamageMultiplierGroupPreview() {
    PindexTheme {
        Surface {
            Column {
                DamageMultiplierGroup("2×", listOf(TypeInfo(5, "ground", LocalizedText("地面", "zh-Hans"))))
                DamageMultiplierGroup("¼×", emptyList())
            }
        }
    }
}
