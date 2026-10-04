package io.github.linxiks.pindex.domain

data class TermMatch(val entityId: Int, val priority: Int)

/**
 * Merges prefix and contains hits: prefix hits rank before contains hits, then by priority,
 * then by entity id. Each entity appears once, at its best (rank, priority).
 */
fun rankMatches(prefix: List<TermMatch>, contains: List<TermMatch>): List<Int> {
    val best = HashMap<Int, Long>(prefix.size + contains.size)
    fun offer(match: TermMatch, rank: Int) {
        // rank in the high bits so a single Long compares (rank, priority).
        val key = (rank.toLong() shl 32) or (match.priority.toLong() and 0xFFFFFFFFL)
        val current = best[match.entityId]
        if (current == null || key < current) best[match.entityId] = key
    }
    prefix.forEach { offer(it, 0) }
    contains.forEach { offer(it, 1) }
    return best.entries
        .sortedWith(compareBy<Map.Entry<Int, Long>> { it.value }.thenBy { it.key })
        .map { it.key }
}
