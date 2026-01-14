package dev.srsouza.hytale.plot.commands.subcommands

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.util.saveLastLocationForTpback
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.Universe

/**
 * /plot visit <player> [index] - Visit another player's plot.
 * Index is 1-based (first plot is 1).
 */
class PlotVisitCommand(private val plugin: PlotPlugin) : CommandBase(
    "visit",
    "Visit another player's plot"
) {
    init {
        requirePermission("souza.plot.command.visit")
    }

    private val playerArg = withRequiredArg(
        "player",
        "souza.plot.command.visit.player.desc",
        ArgTypes.STRING
    )

    private val indexArg = withOptionalArg(
        "index",
        "souza.plot.command.visit.index.desc",
        ArgTypes.STRING
    )

    override fun executeSync(context: CommandContext) {
        if (!context.isPlayer) {
            context.sendMessage(Message.raw(plugin.config.messages.playerOnly))
            return
        }

        val player = context.senderAs(Player::class.java)
        val playerRef = player.playerRef

        val targetName = playerArg.get(context)
        if (targetName.isNullOrBlank()) {
            context.sendMessage(Message.raw("Usage: /plot visit <player> [index]"))
            return
        }

        val targetPlayer = Universe.get().players.find {
            it.username.equals(targetName, ignoreCase = true)
        }

        val targetUuid = targetPlayer?.uuid
        if (targetUuid == null) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.playerNotFound, targetName
            )))
            return
        }

        val targetPlots = plugin.plotManager.getPlayerPlots(targetUuid).toList()
        if (targetPlots.isEmpty()) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.plotNotFound
            )))
            return
        }

        val indexStr = indexArg.get(context)
        val index = if (indexStr.isNullOrBlank()) {
            1
        } else {
            indexStr.toIntOrNull()?.coerceIn(1, targetPlots.size) ?: 1
        }

        if (index < 1 || index > targetPlots.size) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.invalidPlotIndex,
                index,
                targetPlots.size
            )))
            return
        }

        val plotId = targetPlots[index - 1]
        val plot = plugin.plotManager.getPlot(plotId)

        if (plot != null && plot.isDenied(player.uuid!!)) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.youAreDenied
            )))
            return
        }

        player.saveLastLocationForTpback(plugin.plotManager)

        plugin.worldManager.teleportToPlot(playerRef, plotId)

        context.sendMessage(Message.raw(plugin.config.messages.format(
            plugin.config.messages.teleportedToPlot, plotId.toString()
        )))
    }
}
