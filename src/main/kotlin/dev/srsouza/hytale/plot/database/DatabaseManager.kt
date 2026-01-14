package dev.srsouza.hytale.plot.database

import dev.srsouza.hytale.plot.config.DatabaseConfig
import dev.srsouza.hytale.plot.database.tables.PlayerLastLocationsTable
import dev.srsouza.hytale.plot.database.tables.PlotDeniedTable
import dev.srsouza.hytale.plot.database.tables.PlotMembersTable
import dev.srsouza.hytale.plot.database.tables.PlotMergedTable
import dev.srsouza.hytale.plot.database.tables.PlotsTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages the database connection pool and schema.
 */
class DatabaseManager(
    private val config: DatabaseConfig,
    private val dataDir: Path,
    private val logger: Logger? = null
) {
    private lateinit var dataSource: HikariDataSource
    private lateinit var database: Database

    /**
     * Initializes the database connection pool and creates tables if needed.
     */
    fun initialize() {
        logger?.info("Initializing database connection...")

        // Resolve the JDBC URL with the data directory (H2 requires absolute path)
        val absoluteDataDir = dataDir.toAbsolutePath().toString()
        val resolvedJdbcUrl = config.jdbcUrl.replace("./", "$absoluteDataDir/")

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = resolvedJdbcUrl
            driverClassName = config.driverClassName
            if (config.username.isNotEmpty()) {
                username = config.username
            }
            if (config.password.isNotEmpty()) {
                password = config.password
            }
            maximumPoolSize = config.maximumPoolSize
            minimumIdle = config.minimumIdle
            idleTimeout = config.idleTimeout
            connectionTimeout = config.connectionTimeout
            maxLifetime = config.maxLifetime

            // Add H2-specific settings if using H2
            if (config.driverClassName.contains("h2", ignoreCase = true)) {
                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            }
        }

        dataSource = HikariDataSource(hikariConfig)
        database = Database.connect(dataSource)

        // Create tables
        transaction(database) {
            SchemaUtils.create(
                PlotsTable,
                PlotMembersTable,
                PlotDeniedTable,
                PlotMergedTable,
                PlayerLastLocationsTable
            )
        }

        logger?.info("Database initialized successfully")
    }

    /**
     * Gets the database instance for transactions.
     */
    fun getDatabase(): Database = database

    /**
     * Shuts down the database connection pool.
     */
    fun shutdown() {
        logger?.info("Shutting down database connection...")
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
        logger?.info("Database connection closed")
    }

    /**
     * Checks if the database is connected.
     */
    fun isConnected(): Boolean {
        return ::dataSource.isInitialized && !dataSource.isClosed
    }
}
