package dev.srsouza.hytale.plot.commands.subcommands

import dev.srsouza.hytale.plot.PlotPlugin
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player

/**
 * /plot list - List your claimed plots.
 */
class PlotListCommand(private val plugin: PlotPlugin) : CommandBase(
    "list",
    "souza.plot.command.list.desc"
) {


    override fun executeSync(context: CommandContext) {
        if (!context.isPlayer) {
            context.sendMessage(Message.raw(plugin.config.messages.playerOnly))
            return
        }

        val player = context.senderAs(Player::class.java)
        val plots = plugin.plotManager.getPlayerPlots(player.uuid!!)

        if (plots.isEmpty()) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.noPlotOwned
            )))
            return
        }

        context.sendMessage(Message.raw("=== Your Plots (${plots.size}) ==="))

        plots.forEachIndexed { index, plotId ->
            val plot = plugin.plotManager.getPlot(plotId)
            val alias = plot?.alias?.let { " ($it)" } ?: ""
            context.sendMessage(Message.raw("${index + 1}. $plotId$alias"))
        }
    }
}
