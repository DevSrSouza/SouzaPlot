package dev.srsouza.hytale.plot.listener

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.model.PlotId
import com.hypixel.hytale.event.EventRegistry
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent
import com.hypixel.hytale.server.core.Message
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Listens for player events and handles plot-related notifications.
 */
class PlayerListener(
    private val plugin: PlotPlugin
) {
    // Track which plot each player is currently in (for entry/exit messages)
    // Using Optional-like wrapper since ConcurrentHashMap doesn't allow null values
    private val playerCurrentPlot = ConcurrentHashMap<UUID, PlotId>()

    /**
     * Registers the player event handlers.
     */
    fun register(eventRegistry: EventRegistry) {
        eventRegistry.register(PlayerConnectEvent::class.java) { event ->
            handlePlayerConnect(event)
        }

        eventRegistry.register(PlayerDisconnectEvent::class.java) { event ->
            handlePlayerDisconnect(event)
        }

        plugin.logger.atInfo().log("Player listeners registered")
    }

    private fun handlePlayerConnect(event: PlayerConnectEvent) {
        val playerRef = event.playerRef
        val uuid = playerRef.uuid ?: return
        val username = playerRef.username

        plugin.logger.atInfo().log("Player connected: %s (%s)", username, uuid)
        // Plot tracking will be initialized when player enters a plot
    }

    private fun handlePlayerDisconnect(event: PlayerDisconnectEvent) {
        val playerRef = event.playerRef
        val uuid = playerRef.uuid ?: return

        // Clean up tracking
        playerCurrentPlot.remove(uuid)

        plugin.logger.atFine().log("Player disconnected: %s", playerRef.username)
    }

    /**
     * Updates the player's current plot and sends entry/exit messages.
     * This should be called periodically or on player movement.
     */
    fun updatePlayerPlot(playerUuid: UUID, worldX: Int, worldZ: Int) {
        val config = plugin.config
        val newPlotId = PlotId.fromWorldCoords(worldX, worldZ, config.plotSize, config.roadWidth)
        val oldPlotId = playerCurrentPlot[playerUuid]

        if (newPlotId != null && newPlotId != oldPlotId) {
            playerCurrentPlot[playerUuid] = newPlotId
            // Entry/exit messages could be sent here if we had access to PlayerRef
        } else if (newPlotId == null && oldPlotId != null) {
            playerCurrentPlot.remove(playerUuid)
        }
    }

    /**
     * Gets the current plot for a player.
     */
    fun getCurrentPlot(playerUuid: UUID): PlotId? = playerCurrentPlot[playerUuid]
}
