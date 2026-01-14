package dev.srsouza.hytale.plot.manager

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.database.PlayerLastLocationRepository
import dev.srsouza.hytale.plot.database.PlotRepository
import dev.srsouza.hytale.plot.model.PlayerLastLocation
import dev.srsouza.hytale.plot.model.Plot
import dev.srsouza.hytale.plot.model.PlotId
import dev.srsouza.hytale.plot.model.PlotMember
import dev.srsouza.hytale.plot.model.TrustLevel
import dev.srsouza.hytale.plot.util.PlotCoordUtil
import com.hypixel.hytale.server.core.permissions.PermissionsModule
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Result of a plot claim operation.
 */
enum class ClaimResult {
    SUCCESS,
    ALREADY_CLAIMED,
    AT_LIMIT,
    ON_ROAD,
    NO_PERMISSION
}

/**
 * Central manager for all plot operations.
 * Maintains an in-memory cache backed by the database.
 */
class PlotManager(
    private val plugin: PlotPlugin,
    private val repository: PlotRepository,
    private val lastLocationRepository: PlayerLastLocationRepository
) {
    // In-memory cache for fast lookups
    private val plots = ConcurrentHashMap<PlotId, Plot>()
    private val playerPlots = ConcurrentHashMap<UUID, MutableSet<PlotId>>()

    // In-memory cache for player last locations (for quick access)
    private val playerLastLocations = ConcurrentHashMap<UUID, PlayerLastLocation>()

    /**
     * Loads all plots from the database into memory.
     */
    fun loadAllPlots() {
        plugin.logger.atInfo().log("Loading plots from database...")
        val allPlots = repository.getAllPlots()

        plots.clear()
        playerPlots.clear()

        allPlots.forEach { plot ->
            plots[plot.id] = plot
            plot.owner?.let { owner ->
                playerPlots.computeIfAbsent(owner) { ConcurrentHashMap.newKeySet() }.add(plot.id)
            }
        }

        plugin.logger.atInfo().log("Loaded ${plots.size} plots")
    }

    /**
     * Gets a plot by its ID.
     */
    fun getPlot(id: PlotId): Plot? = plots[id]

    /**
     * Gets the plot at the given world coordinates.
     */
    fun getPlotAt(worldX: Int, worldZ: Int): Plot? {
        val config = plugin.config
        val plotId = PlotId.fromWorldCoords(worldX, worldZ, config.plotSize, config.roadWidth)
            ?: return null
        return plots[plotId]
    }

    /**
     * Checks if a plot exists (is claimed).
     */
    fun isPlotClaimed(id: PlotId): Boolean = plots.containsKey(id)

    /**
     * Claims a plot for a player.
     */
    fun claimPlot(id: PlotId, player: UUID): ClaimResult {
        if (isPlotClaimed(id)) {
            return ClaimResult.ALREADY_CLAIMED
        }

        if (!canPlayerClaim(player)) {
            return ClaimResult.AT_LIMIT
        }

        val plot = Plot(
            id = id,
            owner = player,
            createdAt = System.currentTimeMillis(),
            lastActivity = System.currentTimeMillis()
        )

        plots[id] = plot
        playerPlots.computeIfAbsent(player) { ConcurrentHashMap.newKeySet() }.add(id)
        repository.savePlot(plot)

        plugin.logger.atInfo().log("Player $player claimed plot $id")
        return ClaimResult.SUCCESS
    }

    /**
     * Claims a plot for the server (admin plot).
     */
    fun claimAdminPlot(id: PlotId): ClaimResult {
        if (isPlotClaimed(id)) {
            return ClaimResult.ALREADY_CLAIMED
        }

        val plot = Plot(
            id = id,
            owner = null,  // null owner = admin plot
            createdAt = System.currentTimeMillis(),
            lastActivity = System.currentTimeMillis()
        )

        plots[id] = plot
        repository.savePlot(plot)

        plugin.logger.atInfo().log("Admin plot claimed at $id")
        return ClaimResult.SUCCESS
    }

    /**
     * Unclaims a plot (deletes it).
     */
    fun unclaimPlot(id: PlotId): Boolean {
        val plot = plots.remove(id) ?: return false

        plot.owner?.let { owner ->
            playerPlots[owner]?.remove(id)
        }

        repository.deletePlot(id)
        plugin.logger.atInfo().log("Plot $id unclaimed")
        return true
    }

    /**
     * Transfers ownership of a plot to another player.
     */
    fun transferOwnership(id: PlotId, newOwner: UUID): Boolean {
        val plot = plots[id] ?: return false
        val oldOwner = plot.owner

        oldOwner?.let { playerPlots[it]?.remove(id) }

        plot.owner = newOwner
        plot.touch()

        playerPlots.computeIfAbsent(newOwner) { ConcurrentHashMap.newKeySet() }.add(id)

        repository.savePlot(plot)
        plugin.logger.atInfo().log("Plot $id transferred from $oldOwner to $newOwner")
        return true
    }

    /**
     * Gets all plots owned by a player.
     */
    fun getPlayerPlots(player: UUID): Set<PlotId> {
        return playerPlots[player]?.toSet() ?: emptySet()
    }

    /**
     * Gets the count of plots owned by a player.
     */
    fun getPlayerPlotCount(player: UUID): Int {
        return playerPlots[player]?.size ?: 0
    }

    /**
     * Gets the plot limit for a player based on permissions.
     * Checks for permissions like "souza.plot.limit.3" for VIPs, etc.
     * Falls back to defaultPlotLimit if no permission found.
     */
    fun getPlayerPlotLimit(player: UUID): Int {
        val config = plugin.config
        val permissionPrefix = config.plotLimitPermissionPrefix

        for (limit in config.maxPlotLimit downTo 1) {
            val permission = "$permissionPrefix$limit"
            try {
                if (PermissionsModule.get().hasPermission(player, permission)) {
                    return limit
                }
            } catch (e: Exception) {
                // Permission check failed, continue
            }
        }

        // Fall back to default limit
        return config.defaultPlotLimit
    }

    /**
     * Checks if a player can claim more plots.
     */
    fun canPlayerClaim(player: UUID): Boolean {
        val currentCount = getPlayerPlotCount(player)
        val limit = getPlayerPlotLimit(player)
        return currentCount < limit
    }

    /**
     * Finds an unclaimed plot using spiral search from origin.
     */
    fun findUnclaimedPlot(): PlotId? {
        val maxDistance = 100  // Search up to 100 plots from origin
        return PlotCoordUtil.spiralFromOrigin(maxDistance)
            .firstOrNull { !isPlotClaimed(it) }
    }

    /**
     * Finds an unclaimed plot near a specific location.
     */
    fun findUnclaimedPlotNear(center: PlotId, radius: Int): PlotId? {
        for (distance in 0..radius) {
            for (dx in -distance..distance) {
                for (dz in -distance..distance) {
                    if (kotlin.math.abs(dx) == distance || kotlin.math.abs(dz) == distance) {
                        val candidate = PlotId(center.x + dx, center.z + dz)
                        if (!isPlotClaimed(candidate)) {
                            return candidate
                        }
                    }
                }
            }
        }
        return null
    }

    /**
     * Adds a member to a plot.
     */
    fun addMember(id: PlotId, member: UUID, trustLevel: TrustLevel): Boolean {
        val plot = plots[id] ?: return false
        if (!plot.addMember(member, trustLevel)) return false

        repository.savePlot(plot)
        return true
    }

    /**
     * Removes a member from a plot.
     */
    fun removeMember(id: PlotId, member: UUID): Boolean {
        val plot = plots[id] ?: return false
        if (!plot.removeMember(member)) return false

        repository.savePlot(plot)
        return true
    }

    /**
     * Denies a player from a plot.
     */
    fun denyPlayer(id: PlotId, player: UUID): Boolean {
        val plot = plots[id] ?: return false
        if (!plot.denyPlayer(player)) return false

        repository.savePlot(plot)
        return true
    }

    /**
     * Un-denies a player from a plot.
     */
    fun undenyPlayer(id: PlotId, player: UUID): Boolean {
        val plot = plots[id] ?: return false
        if (!plot.undenyPlayer(player)) return false

        repository.savePlot(plot)
        return true
    }

    /**
     * Updates a plot's settings.
     */
    fun updatePlot(plot: Plot) {
        plot.touch()
        plots[plot.id] = plot
        repository.savePlot(plot)
    }

    /**
     * Gets all admin plots.
     */
    fun getAdminPlots(): Set<PlotId> {
        return plots.values.filter { it.isAdminPlot() }.map { it.id }.toSet()
    }

    /**
     * Gets the total number of claimed plots.
     */
    fun getTotalPlotCount(): Int = plots.size

    // ==================== Last Location Tracking ====================

    /**
     * Saves a player's last location before teleporting to a plot.
     */
    fun savePlayerLastLocation(location: PlayerLastLocation) {
        playerLastLocations[location.playerUuid] = location
        lastLocationRepository.saveLastLocation(location)
    }

    /**
     * Gets a player's last saved location.
     */
    fun getPlayerLastLocation(playerUuid: UUID): PlayerLastLocation? {
        playerLastLocations[playerUuid]?.let { return it }

        return lastLocationRepository.getLastLocation(playerUuid)?.also {
            playerLastLocations[playerUuid] = it
        }
    }

    /**
     * Checks if a player has a saved last location.
     */
    fun hasPlayerLastLocation(playerUuid: UUID): Boolean {
        return playerLastLocations.containsKey(playerUuid) ||
                lastLocationRepository.hasLastLocation(playerUuid)
    }

    /**
     * Clears a player's last location after they've used tpback.
     */
    fun clearPlayerLastLocation(playerUuid: UUID) {
        playerLastLocations.remove(playerUuid)
        lastLocationRepository.deleteLastLocation(playerUuid)
    }
}
