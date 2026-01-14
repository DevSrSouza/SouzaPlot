package dev.srsouza.hytale.plot.commands.subcommands

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.model.PlotId
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.Universe

/**
 * /plot undeny <player> - Allow a denied player again.
 */
class PlotUndenyCommand(private val plugin: PlotPlugin) : CommandBase(
    "undeny",
    "Remove a player from the deny list"
) {
    init {
        requirePermission("souza.plot.command.undeny")
    }

    private val playerArg = withRequiredArg(
        "player",
        "souza.plot.command.undeny.player.desc",
        ArgTypes.STRING
    )

    override fun executeSync(context: CommandContext) {
        if (!context.isPlayer) {
            context.sendMessage(Message.raw(plugin.config.messages.playerOnly))
            return
        }

        val player = context.senderAs(Player::class.java)

        val targetName = playerArg.get(context)
        if (targetName.isNullOrBlank()) {
            context.sendMessage(Message.raw("Usage: /plot undeny <player>"))
            return
        }

        val world = player.world
        if (world == null || !plugin.worldManager.isPlotWorld(world)) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.notInPlotWorld
            )))
            return
        }

        // Get current plot
        val position = player.playerRef.transform.position
        val plotId = PlotId.fromWorldCoords(
            position.x.toInt(),
            position.z.toInt(),
            plugin.config.plotSize,
            plugin.config.roadWidth
        )

        if (plotId == null) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.cannotClaimRoad
            )))
            return
        }

        val plot = plugin.plotManager.getPlot(plotId)
        if (plot == null || !plot.isOwner(player.uuid!!)) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.noPermission
            )))
            return
        }

        // Find target player
        val targetPlayer = Universe.get().players.find {
            it.username.equals(targetName, ignoreCase = true)
        }

        if (targetPlayer == null) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.playerNotFound, targetName
            )))
            return
        }

        // Undeny player
        if (plugin.plotManager.undenyPlayer(plotId, targetPlayer.uuid)) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.playerUndenied, targetPlayer.username
            )))
        } else {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.playerNotDenied, targetPlayer.username
            )))
        }
    }
}
