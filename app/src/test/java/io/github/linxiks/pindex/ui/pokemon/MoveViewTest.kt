package io.github.linxiks.pindex.ui.pokemon

import io.github.linxiks.pindex.data.model.LearnedMove
import io.github.linxiks.pindex.data.model.Learnset
import io.github.linxiks.pindex.data.model.TypeInfo
import io.github.linxiks.pindex.data.model.VersionGroupOption
import io.github.linxiks.pindex.domain.LocalizedText
import io.github.linxiks.pindex.domain.MoveMethodGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoveViewTest {
    private val electric = TypeInfo(13, "electric", LocalizedText("电", "zh-Hans"))
    private val special = LocalizedText("特殊", "zh-Hans")

    private fun move(id: Int, vg: Int, group: MoveMethodGroup, level: Int = 0) =
        LearnedMove(id, vg, group, level, LocalizedText("m$id", "zh-Hans"), electric, special, null)

    // vg 25: LevelUp + Machine; vg 20: Machine only.
    private val learnset = Learnset(
        versionGroups = listOf(VersionGroupOption(25, "scarlet-violet", emptyList()), VersionGroupOption(20, "sword-shield", emptyList())),
        defaultVersionGroupId = 25,
        moves = listOf(
            move(609, 25, MoveMethodGroup.LevelUp, 1),
            move(85, 25, MoveMethodGroup.Machine),
            move(86, 20, MoveMethodGroup.Machine),
        ),
    )

    @Test
    fun unknownVersionGroupFallsBackToDefault() {
        val view = checkNotNull(resolveMoveView(learnset, MoveSelection(versionGroupId = 999)))
        assertEquals(25, view.versionGroupId)
        assertEquals(MoveMethodGroup.LevelUp, view.group)
        assertEquals(listOf(609), view.moves.map { it.moveId })
    }

    @Test
    fun emptyMethodFallsBackToFirstNonEmpty() {
        val view = checkNotNull(resolveMoveView(learnset, MoveSelection(20, MoveMethodGroup.Egg)))
        assertEquals(20, view.versionGroupId)
        assertEquals(listOf(MoveMethodGroup.Machine), view.groups)
        assertEquals(MoveMethodGroup.Machine, view.group)
        assertEquals(listOf(86), view.moves.map { it.moveId })
    }

    @Test
    fun emptyLearnsetHasNoView() {
        assertNull(resolveMoveView(Learnset(emptyList(), null, emptyList()), MoveSelection()))
    }
}
