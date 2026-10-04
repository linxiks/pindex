package io.github.linxiks.pindex.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.ui.preview.SampleData

/** Bar length scale: the fixed maximum base stat. */
private const val STAT_MAX = 255f

private val LabelMinWidth = 40.dp
private val ValueMinWidth = 36.dp
private val BarShape = RoundedCornerShape(4.dp)

/** One base-stat row. The bar always uses primary (not the type color) to keep contrast in dark mode. */
@Composable
fun StatBar(label: String, value: Int, modifier: Modifier = Modifier) {
    StatRow(label = label, value = value, modifier = modifier) {
        Box(
            Modifier
                .weight(1f)
                .height(8.dp)
                .clip(BarShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((value / STAT_MAX).coerceIn(0f, 1f))
                    .clip(BarShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

/** Sum row without a bar. */
@Composable
fun StatTotalRow(label: String, value: Int, modifier: Modifier = Modifier) {
    StatRow(label = label, value = value, modifier = modifier) {}
}

@Composable
private fun StatRow(
    label: String,
    value: Int,
    modifier: Modifier,
    bar: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.widthIn(min = LabelMinWidth),
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = ValueMinWidth),
        )
        bar()
    }
}

@PreviewLightDark
@Composable
private fun StatBarPreview() {
    PindexTheme {
        Surface {
            Column(Modifier.padding(Spacing.l)) {
                SampleData.pikachu.stats.forEach { StatBar(it.label.text, it.value) }
                StatTotalRow("总和", SampleData.pikachu.stats.sumOf { it.value })
            }
        }
    }
}
