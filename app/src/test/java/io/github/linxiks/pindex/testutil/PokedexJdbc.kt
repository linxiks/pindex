package io.github.linxiks.pindex.testutil

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

/**
 * Read-only JDBC access to data/generated/pokedex.db (path injected by app/build.gradle.kts).
 * A missing database fails the test instead of skipping it.
 */
object PokedexJdbc {
    private val connection: Connection by lazy {
        val path = System.getProperty("pokedex.db")
            ?: error("system property pokedex.db is not set; run through Gradle")
        if (!File(path).exists()) error("$path 不存在：先运行 tools/data-builder/build.py")
        DriverManager.getConnection("jdbc:sqlite:file:$path?mode=ro")
    }

    private val ParamPattern = Regex(":(\\w+)")

    /** Runs [sql] with Room-style `:name` parameters; List values expand to `?, ?, …`. */
    fun <T> query(sql: String, params: Map<String, Any> = emptyMap(), map: (ResultSet) -> T): List<T> {
        val bound = ArrayList<Any>()
        val jdbcSql = ParamPattern.replace(sql) { m ->
            val value = params[m.groupValues[1]] ?: error("missing parameter ${m.value} for: $sql")
            if (value is List<*>) {
                value.forEach { bound.add(it!!) }
                List(value.size) { "?" }.joinToString(", ")
            } else {
                bound.add(value)
                "?"
            }
        }
        connection.prepareStatement(jdbcSql).use { stmt ->
            bound.forEachIndexed { i, v -> stmt.setObject(i + 1, v) }
            stmt.executeQuery().use { rs ->
                val out = ArrayList<T>()
                while (rs.next()) out.add(map(rs))
                return out
            }
        }
    }

    fun userVersion(): Int = query("PRAGMA user_version") { it.getInt(1) }.single()
}

fun ResultSet.getIntOrNull(column: String): Int? = getInt(column).takeUnless { wasNull() }

fun ResultSet.getBool(column: String): Boolean = getInt(column) != 0
