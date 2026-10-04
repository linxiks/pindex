package io.github.linxiks.pindex.domain

/** Tabs of design.md §30, in display order. */
enum class MoveMethodGroup { LevelUp, Machine, Egg, Other }

fun moveMethodGroup(methodIdentifier: String): MoveMethodGroup = when (methodIdentifier) {
    "level-up" -> MoveMethodGroup.LevelUp
    "machine" -> MoveMethodGroup.Machine
    "egg" -> MoveMethodGroup.Egg
    else -> MoveMethodGroup.Other
}
