package io.github.linxiks.pindex.ui.pokemon

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import io.github.linxiks.pindex.R
import io.github.linxiks.pindex.core.components.MoveItem
import io.github.linxiks.pindex.core.components.SectionHeader
import io.github.linxiks.pindex.core.components.TypeUi
import io.github.linxiks.pindex.core.components.localizedAnnotated
import io.github.linxiks.pindex.core.theme.Spacing
import io.github.linxiks.pindex.data.model.LearnedMove
import io.github.linxiks.pindex.data.model.Learnset
import io.github.linxiks.pindex.data.model.VersionGroupOption
import io.github.linxiks.pindex.domain.MoveMethodGroup

/** What the move section shows: the effective version group and method after fallback. */
internal data class MoveView(
    val versionGroupId: Int,
    /** Methods with at least one move in [versionGroupId], in enum order. */
    val groups: List<MoveMethodGroup>,
    val group: MoveMethodGroup,
    val moves: List<LearnedMove>,
)

/**
 * Applies [selection] to [learnset]; a version group or method that has no moves falls back to
 * the default group / first non-empty method. Null when the pokemon learns nothing.
 */
internal fun resolveMoveView(learnset: Learnset, selection: MoveSelection): MoveView? {
    if (learnset.moves.isEmpty()) return null
    val versionGroupId = selection.versionGroupId?.takeIf { id -> learnset.versionGroups.any { it.id == id } }
        ?: learnset.defaultVersionGroupId
        ?: return null
    val inGroup = learnset.moves.filter { it.versionGroupId == versionGroupId }
    val present = inGroup.mapTo(HashSet()) { it.group }
    val groups = MoveMethodGroup.entries.filter { it in present }
    if (groups.isEmpty()) return null
    val group = selection.group.takeIf { it in present } ?: groups.first()
    return MoveView(versionGroupId, groups, group, inGroup.filter { it.group == group })
}

/** Moves header, version picker, method tabs, then one lazy item per move. */
internal fun LazyListScope.movesSection(
    learnset: Learnset,
    view: MoveView?,
    onSelectVersionGroup: (Int) -> Unit,
    onSelectGroup: (MoveMethodGroup) -> Unit,
    onOpenMove: (Int) -> Unit,
) {
    item(key = "movesHeader") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            SectionHeader(stringResource(R.string.section_moves))
            if (view == null) {
                Text(
                    text = stringResource(R.string.moves_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }
            Text(
                text = stringResource(R.string.move_version_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VersionGroupPicker(learnset.versionGroups, view.versionGroupId, onSelectVersionGroup)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                view.groups.forEach { g ->
                    FilterChip(
                        selected = g == view.group,
                        onClick = { onSelectGroup(g) },
                        label = { Text(stringResource(groupLabel(g))) },
                        shape = MaterialTheme.shapes.small,
                    )
                }
            }
        }
    }
    if (view == null) return
    items(view.moves, key = { "move-${it.versionGroupId}-${it.group}-${it.moveId}-${it.level}" }) {
        MoveItem(
            leading = when {
                it.group != MoveMethodGroup.LevelUp -> null
                it.level == 0 -> stringResource(R.string.move_level_evolve)
                else -> stringResource(R.string.move_level, it.level)
            },
            name = it.name,
            type = TypeUi(it.type.name.text, it.type.identifier),
            damageClass = it.damageClass.text,
            power = it.power,
            onClick = { onOpenMove(it.moveId) },
        )
    }
}

@StringRes
private fun groupLabel(group: MoveMethodGroup): Int = when (group) {
    MoveMethodGroup.LevelUp -> R.string.move_group_level_up
    MoveMethodGroup.Machine -> R.string.move_group_machine
    MoveMethodGroup.Egg -> R.string.move_group_egg
    MoveMethodGroup.Other -> R.string.move_group_other
}

@Composable
private fun VersionGroupPicker(options: List<VersionGroupOption>, selectedId: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, shape = MaterialTheme.shapes.small) {
            Text(versionGroupLabel(options.firstOrNull { it.id == selectedId }))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(versionGroupLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun versionGroupLabel(option: VersionGroupOption?): AnnotatedString {
    if (option == null) return AnnotatedString("")
    if (option.versions.isEmpty()) return AnnotatedString(option.identifier)
    return AnnotatedString.Builder().apply {
        option.versions.forEachIndexed { i, v ->
            if (i > 0) append(" / ")
            append(localizedAnnotated(v))
        }
    }.toAnnotatedString()
}
