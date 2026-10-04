package io.github.linxiks.pindex.data.repository

import io.github.linxiks.pindex.data.local.MetaDao
import io.github.linxiks.pindex.data.model.DataVersion

class MetaRepository(private val dao: MetaDao) {
    suspend fun dataVersion(): DataVersion {
        val meta = dao.all().associate { it.key to it.value }
        return DataVersion(
            dataVersion = meta["data_version"].orEmpty(),
            buildDate = meta["build_date"].orEmpty(),
        )
    }
}
