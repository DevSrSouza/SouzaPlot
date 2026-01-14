package dev.srsouza.hytale.plot.manager

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.model.PlotId
import com.hypixel.hytale.server.core.permissions.PermissionsModule
import java.util.UUID

/**
 * Manages block protection checks for plots.
 */
class PlotProtectionManager(
    private val plugin: PlotPlugin
) {
    companion object {
        const val PERMISSION_ADMIN_BYPASS = "souza.plot.admin.bypass"
        const val PERMISSION_BUILD_ROADS = "souza.plot.admin.roads"
    }

    /**
     * Checks if a player can modify blocks at a position.
     * @return true if allowed, false if denied
     */
    fun canModify(playerUuid: UUID, worldX: Int, worldY: Int, worldZ: Int): Boolean {
        // Admin bypass
        if (hasAdminBypass(playerUuid)) return true

        val config = plugin.config

        // Check bottom layer protection (prevents void deaths)
        if (config.protectBottomLayer && worldY <= config.protectedLayerHeight) {
            return false
        }

        // Check if on road
        if (PlotId.isOnRoad(worldX, worldZ, config.plotSize, config.roadWidth)) {
            // Roads are protected unless player has road build permission
            return hasPermission(playerUuid, PERMISSION_BUILD_ROADS)
        }

        // Check if on border - borders are always protected (only admins can modify)
        if (config.borderEnabled && PlotId.isOnBorder(worldX, worldZ, config.plotSize, config.roadWidth)) {
            return hasPermission(playerUuid, PERMISSION_BUILD_ROADS)
        }

        // Get plot at position
        val plotId = PlotId.fromWorldCoords(worldX, worldZ, config.plotSize, config.roadWidth)
            ?: return false  // Should not happen after road check, but safety first

        // Get plot data
        val plot = plugin.plotManager.getPlot(plotId)

        // Unclaimed plots cannot be modified
        if (plot == null) return false

        // Check if player can build on this plot
        return plot.canBuild(playerUuid)
    }

    /**
     * Checks if a block position is on the protected bottom layer.
     */
    fun isProtectedBottomLayer(worldY: Int): Boolean {
        val config = plugin.config
        return config.protectBottomLayer && worldY <= config.protectedLayerHeight
    }

    /**
     * Checks if a player can interact (use containers, buttons, etc.) at a position.
     */
    fun canInteract(playerUuid: UUID, worldX: Int, worldY: Int, worldZ: Int): Boolean {
        // Admin bypass
        if (hasAdminBypass(playerUuid)) return true

        val config = plugin.config

        // Roads allow interaction
        if (PlotId.isOnRoad(worldX, worldZ, config.plotSize, config.roadWidth)) {
            return true
        }

        // Get plot at position
        val plotId = PlotId.fromWorldCoords(worldX, worldZ, config.plotSize, config.roadWidth)
            ?: return true

        val plot = plugin.plotManager.getPlot(plotId)

        // Unclaimed plots allow interaction
        if (plot == null) return true

        // Check if player can use containers
        return plot.canUseContainers(playerUuid)
    }

    /**
     * Checks if a player can enter a plot.
     */
    fun canEnter(playerUuid: UUID, plotId: PlotId): Boolean {
        // Admin bypass
        if (hasAdminBypass(playerUuid)) return true

        val plot = plugin.plotManager.getPlot(plotId)

        // Unclaimed or no plot = can enter
        if (plot == null) return true

        // Check if denied
        return !plot.isDenied(playerUuid)
    }

    /**
     * Returns whether a position is on a road.
     */
    fun isRoad(worldX: Int, worldZ: Int): Boolean {
        val config = plugin.config
        return PlotId.isOnRoad(worldX, worldZ, config.plotSize, config.roadWidth)
    }

    /**
     * Checks if player has admin bypass permission.
     */
    fun hasAdminBypass(playerUuid: UUID): Boolean {
        return hasPermission(playerUuid, PERMISSION_ADMIN_BYPASS)
    }

    /**
     * Helper to check permissions.
     */
    private fun hasPermission(playerUuid: UUID, permission: String): Boolean {
        return try {
            PermissionsModule.get().hasPermission(playerUuid, permission)
        } catch (e: Exception) {
            false
        }
    }
}
