package dev.srsouza.hytale.plot.commands.subcommands

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.util.saveLastLocationForTpback
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player

/**
 * /plot home [index] - Teleport to your plot by index (1-based).
 * If no index is provided, teleports to the first plot.
 */
class PlotHomeCommand(private val plugin: PlotPlugin) : CommandBase(
    "home",
    "souza.plot.command.home.desc"
) {

    private val indexArg = withOptionalArg(
        "index",
        "souza.plot.command.home.index.desc",
        ArgTypes.STRING
    )


    override fun executeSync(context: CommandContext) {
        if (!context.isPlayer) {
            context.sendMessage(Message.raw(plugin.config.messages.playerOnly))
            return
        }

        val player = context.senderAs(Player::class.java)
        val playerRef = player.playerRef

        val plots = plugin.plotManager.getPlayerPlots(player.uuid!!).toList()
        if (plots.isEmpty()) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.noPlotOwned
            )))
            return
        }

        val indexStr = indexArg.get(context)
        val index = if (indexStr.isNullOrBlank()) {
            1
        } else {
            indexStr.toIntOrNull()?.coerceIn(1, plots.size) ?: 1
        }

        if (index < 1 || index > plots.size) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.invalidPlotIndex,
                index,
                plots.size
            )))
            return
        }

        val plotId = plots[index - 1]
        val plot = plugin.plotManager.getPlot(plotId)

        player.saveLastLocationForTpback(plugin.plotManager)

        plugin.worldManager.teleportToPlotHome(playerRef, plotId, plot?.homeLocation)

        if (plots.size > 1) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.teleportedToHomeIndex,
                index,
                plots.size
            )))
        } else {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.teleportedToHome
            )))
        }
    }
}
