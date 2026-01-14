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
 * /plot visit <player> - Visit another player's plot.
 */
class PlotVisitCommand(private val plugin: PlotPlugin) : CommandBase(
    "visit",
    "souza.plot.command.visit.desc"
) {

    private val playerArg = withRequiredArg(
        "player",
        "souza.plot.command.visit.player.desc",
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
            context.sendMessage(Message.raw("Usage: /plot visit <player>"))
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

        val targetPlots = plugin.plotManager.getPlayerPlots(targetUuid)
        if (targetPlots.isEmpty()) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.plotNotFound
            )))
            return
        }

        val plotId = targetPlots.first()
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
