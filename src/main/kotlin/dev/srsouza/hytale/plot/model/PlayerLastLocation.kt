package dev.srsouza.hytale.plot.model

import java.util.UUID

/**
 * Represents a player's last location before teleporting to a plot.
 * Used by /plot tpback to return players to their previous location.
 */
data class PlayerLastLocation(
    val playerUuid: UUID,
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
    val savedAt: Long = System.currentTimeMillis()
)
