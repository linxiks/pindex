package io.github.linxiks.pindex.domain

/** generationId 0 means "all generations". */
fun <T> filterByGeneration(items: List<T>, generationId: Int, generationOf: (T) -> Int): List<T> =
    if (generationId == 0) items else items.filter { generationOf(it) == generationId }
