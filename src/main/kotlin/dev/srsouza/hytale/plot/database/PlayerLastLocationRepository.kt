package dev.srsouza.hytale.plot.database

import dev.srsouza.hytale.plot.database.tables.PlayerLastLocationsTable
import dev.srsouza.hytale.plot.model.PlayerLastLocation
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

/**
 * Repository for player last location data access operations.
 */
class PlayerLastLocationRepository(private val database: Database) {

    /**
     * Gets a player's last location.
     */
    fun getLastLocation(playerUuid: UUID): PlayerLastLocation? = transaction(database) {
        PlayerLastLocationsTable.selectAll()
            .where { PlayerLastLocationsTable.playerUuid eq playerUuid }
            .firstOrNull()?.let { row ->
                PlayerLastLocation(
                    playerUuid = row[PlayerLastLocationsTable.playerUuid],
                    worldName = row[PlayerLastLocationsTable.worldName],
                    x = row[PlayerLastLocationsTable.x],
                    y = row[PlayerLastLocationsTable.y],
                    z = row[PlayerLastLocationsTable.z],
                    yaw = row[PlayerLastLocationsTable.yaw],
                    pitch = row[PlayerLastLocationsTable.pitch],
                    savedAt = row[PlayerLastLocationsTable.savedAt]
                )
            }
    }

    /**
     * Saves a player's last location (insert or update).
     */
    fun saveLastLocation(location: PlayerLastLocation) = transaction(database) {
        PlayerLastLocationsTable.upsert {
            it[playerUuid] = location.playerUuid
            it[worldName] = location.worldName
            it[x] = location.x
            it[y] = location.y
            it[z] = location.z
            it[yaw] = location.yaw
            it[pitch] = location.pitch
            it[savedAt] = location.savedAt
        }
    }

    /**
     * Deletes a player's last location.
     */
    fun deleteLastLocation(playerUuid: UUID) = transaction(database) {
        PlayerLastLocationsTable.deleteWhere { PlayerLastLocationsTable.playerUuid eq playerUuid }
    }

    /**
     * Checks if a player has a saved last location.
     */
    fun hasLastLocation(playerUuid: UUID): Boolean = transaction(database) {
        PlayerLastLocationsTable.selectAll()
            .where { PlayerLastLocationsTable.playerUuid eq playerUuid }
            .count() > 0
    }
}
