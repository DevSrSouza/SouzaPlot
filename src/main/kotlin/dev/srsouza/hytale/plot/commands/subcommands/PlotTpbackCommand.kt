package dev.srsouza.hytale.plot.commands.subcommands

import dev.srsouza.hytale.plot.PlotPlugin
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.Universe

/**
 * /plot tpback - Teleport back to your previous location before using /plot auto or /plot home.
 */
class PlotTpbackCommand(private val plugin: PlotPlugin) : CommandBase(
    "tpback",
    "Teleport back to your previous location"
) {
    init {
        requirePermission("souza.plot.command.tpback")
    }

    override fun executeSync(context: CommandContext) {
        if (!context.isPlayer) {
            context.sendMessage(Message.raw(plugin.config.messages.playerOnly))
            return
        }

        val player = context.senderAs(Player::class.java)
        val playerRef = player.playerRef

        val lastLocation = plugin.plotManager.getPlayerLastLocation(player.uuid!!)
        if (lastLocation == null) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.noLastLocation
            )))
            return
        }

        val targetWorld = Universe.get().getWorld(lastLocation.worldName)
        if (targetWorld == null) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.lastLocationWorldNotFound
            )))
            return
        }

        val position = Vector3d(lastLocation.x, lastLocation.y, lastLocation.z)
        val rotation = Vector3f(lastLocation.pitch, lastLocation.yaw, 0f)
        plugin.worldManager.teleportToLocation(playerRef, targetWorld, position, rotation)

        plugin.plotManager.clearPlayerLastLocation(player.uuid!!)

        context.sendMessage(Message.raw(plugin.config.messages.format(
            plugin.config.messages.teleportedBack
        )))
    }
}
