package dev.srsouza.hytale.plot.util

import dev.srsouza.hytale.plot.manager.PlotManager
import dev.srsouza.hytale.plot.model.PlayerLastLocation
import com.hypixel.hytale.server.core.entity.entities.Player

/**
 * Extension functions for Player entity.
 */

/**
 * Saves the player's current location before teleporting.
 * This allows the player to use /plot tpback to return to this location.
 *
 * @param plotManager The PlotManager instance to save the location
 */
fun Player.saveLastLocationForTpback(plotManager: PlotManager) {
    val world = this.world ?: return
    val playerRef = this.playerRef ?: return
    val uuid = this.uuid ?: return

    val position = playerRef.transform.position
    val rotation = playerRef.transform.rotation

    plotManager.savePlayerLastLocation(
        PlayerLastLocation(
            playerUuid = uuid,
            worldName = world.name,
            x = position.x,
            y = position.y,
            z = position.z,
            yaw = rotation.y,
            pitch = rotation.x
        )
    )
}
