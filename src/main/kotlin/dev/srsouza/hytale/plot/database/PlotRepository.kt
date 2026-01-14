package dev.srsouza.hytale.plot.database

import dev.srsouza.hytale.plot.database.tables.PlotDeniedTable
import dev.srsouza.hytale.plot.database.tables.PlotMembersTable
import dev.srsouza.hytale.plot.database.tables.PlotMergedTable
import dev.srsouza.hytale.plot.database.tables.PlotsTable
import dev.srsouza.hytale.plot.model.Plot
import dev.srsouza.hytale.plot.model.PlotId
import dev.srsouza.hytale.plot.model.PlotMember
import dev.srsouza.hytale.plot.model.PlotSettings
import dev.srsouza.hytale.plot.model.TrustLevel
import com.hypixel.hytale.math.vector.Vector3d
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

/**
 * Repository for plot data access operations.
 */
class PlotRepository(private val database: Database) {

    /**
     * Gets a plot by its ID.
     */
    fun getPlot(id: PlotId): Plot? = transaction(database) {
        val row = PlotsTable.selectAll()
            .where { (PlotsTable.plotX eq id.x) and (PlotsTable.plotZ eq id.z) }
            .firstOrNull() ?: return@transaction null

        rowToPlot(row, id)
    }

    /**
     * Saves a plot (insert or update).
     */
    fun savePlot(plot: Plot) = transaction(database) {
        PlotsTable.upsert {
            it[plotX] = plot.id.x
            it[plotZ] = plot.id.z
            it[owner] = plot.owner
            it[alias] = plot.alias
            it[homeX] = plot.homeLocation?.x
            it[homeY] = plot.homeLocation?.y
            it[homeZ] = plot.homeLocation?.z
            it[pvp] = plot.settings.pvp
            it[mobSpawning] = plot.settings.mobSpawning
            it[weather] = plot.settings.weather
            it[fixedTime] = plot.settings.fixedTime
            it[entryMessage] = plot.settings.entryMessage
            it[exitMessage] = plot.settings.exitMessage
            it[gameMode] = plot.settings.gameMode
            it[createdAt] = plot.createdAt
            it[lastActivity] = plot.lastActivity
        }

        PlotMembersTable.deleteWhere {
            (plotX eq plot.id.x) and (plotZ eq plot.id.z)
        }
        plot.members.forEach { member ->
            PlotMembersTable.insert {
                it[plotX] = plot.id.x
                it[plotZ] = plot.id.z
                it[memberUuid] = member.uuid
                it[trustLevel] = member.trustLevel.name
                it[addedAt] = member.addedAt
            }
        }

        PlotDeniedTable.deleteWhere {
            (plotX eq plot.id.x) and (plotZ eq plot.id.z)
        }
        plot.denied.forEach { uuid ->
            PlotDeniedTable.insert {
                it[plotX] = plot.id.x
                it[plotZ] = plot.id.z
                it[deniedUuid] = uuid
            }
        }

        PlotMergedTable.deleteWhere {
            (plotX eq plot.id.x) and (plotZ eq plot.id.z)
        }
        plot.mergedWith.forEach { mergedId ->
            PlotMergedTable.insert {
                it[plotX] = plot.id.x
                it[plotZ] = plot.id.z
                it[mergedX] = mergedId.x
                it[mergedZ] = mergedId.z
            }
        }
    }

    /**
     * Deletes a plot and all associated data.
     */
    fun deletePlot(id: PlotId) = transaction(database) {
        PlotMembersTable.deleteWhere { (plotX eq id.x) and (plotZ eq id.z) }
        PlotDeniedTable.deleteWhere { (plotX eq id.x) and (plotZ eq id.z) }
        PlotMergedTable.deleteWhere { (plotX eq id.x) and (plotZ eq id.z) }
        PlotsTable.deleteWhere { (plotX eq id.x) and (plotZ eq id.z) }
    }

    /**
     * Gets all plots from the database.
     */
    fun getAllPlots(): List<Plot> = transaction(database) {
        PlotsTable.selectAll().map { row ->
            val id = PlotId(row[PlotsTable.plotX], row[PlotsTable.plotZ])
            rowToPlot(row, id)
        }
    }

    /**
     * Gets all plots owned by a player.
     */
    fun getPlayerPlots(uuid: UUID): List<Plot> = transaction(database) {
        PlotsTable.selectAll()
            .where { PlotsTable.owner eq uuid }
            .map { row ->
                val id = PlotId(row[PlotsTable.plotX], row[PlotsTable.plotZ])
                rowToPlot(row, id)
            }
    }

    /**
     * Gets the count of plots owned by a player.
     */
    fun getPlayerPlotCount(uuid: UUID): Int = transaction(database) {
        PlotsTable.selectAll()
            .where { PlotsTable.owner eq uuid }
            .count().toInt()
    }

    /**
     * Gets all admin (server) plots.
     */
    fun getAdminPlots(): List<Plot> = transaction(database) {
        PlotsTable.selectAll()
            .where { PlotsTable.owner.isNull() }
            .map { row ->
                val id = PlotId(row[PlotsTable.plotX], row[PlotsTable.plotZ])
                rowToPlot(row, id)
            }
    }

    /**
     * Checks if a plot exists.
     */
    fun plotExists(id: PlotId): Boolean = transaction(database) {
        PlotsTable.selectAll()
            .where { (PlotsTable.plotX eq id.x) and (PlotsTable.plotZ eq id.z) }
            .count() > 0
    }

    /**
     * Gets members for a plot.
     */
    private fun getMembersForPlot(id: PlotId): MutableSet<PlotMember> {
        return PlotMembersTable.selectAll()
            .where { (PlotMembersTable.plotX eq id.x) and (PlotMembersTable.plotZ eq id.z) }
            .map { row ->
                PlotMember(
                    uuid = row[PlotMembersTable.memberUuid],
                    trustLevel = TrustLevel.valueOf(row[PlotMembersTable.trustLevel]),
                    addedAt = row[PlotMembersTable.addedAt]
                )
            }.toMutableSet()
    }

    /**
     * Gets denied players for a plot.
     */
    private fun getDeniedForPlot(id: PlotId): MutableSet<UUID> {
        return PlotDeniedTable.selectAll()
            .where { (PlotDeniedTable.plotX eq id.x) and (PlotDeniedTable.plotZ eq id.z) }
            .map { row -> row[PlotDeniedTable.deniedUuid] }
            .toMutableSet()
    }

    /**
     * Gets merged plots for a plot.
     */
    private fun getMergedForPlot(id: PlotId): MutableSet<PlotId> {
        return PlotMergedTable.selectAll()
            .where { (PlotMergedTable.plotX eq id.x) and (PlotMergedTable.plotZ eq id.z) }
            .map { row -> PlotId(row[PlotMergedTable.mergedX], row[PlotMergedTable.mergedZ]) }
            .toMutableSet()
    }

    /**
     * Converts a database row to a Plot object.
     */
    private fun rowToPlot(row: ResultRow, id: PlotId): Plot {
        val homeX = row[PlotsTable.homeX]
        val homeY = row[PlotsTable.homeY]
        val homeZ = row[PlotsTable.homeZ]
        val homeLocation = if (homeX != null && homeY != null && homeZ != null) {
            Vector3d(homeX, homeY, homeZ)
        } else null

        return Plot(
            id = id,
            owner = row[PlotsTable.owner],
            members = getMembersForPlot(id),
            denied = getDeniedForPlot(id),
            settings = PlotSettings(
                pvp = row[PlotsTable.pvp],
                mobSpawning = row[PlotsTable.mobSpawning],
                weather = row[PlotsTable.weather],
                fixedTime = row[PlotsTable.fixedTime],
                entryMessage = row[PlotsTable.entryMessage],
                exitMessage = row[PlotsTable.exitMessage],
                gameMode = row[PlotsTable.gameMode]
            ),
            homeLocation = homeLocation,
            alias = row[PlotsTable.alias],
            mergedWith = getMergedForPlot(id),
            createdAt = row[PlotsTable.createdAt],
            lastActivity = row[PlotsTable.lastActivity]
        )
    }
}
