package dev.srsouza.hytale.plot.commands.subcommands

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.manager.ClaimResult
import dev.srsouza.hytale.plot.util.saveLastLocationForTpback
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player

/**
 * /plot auto - Automatically claim a random unclaimed plot and teleport to it.
 */
class PlotAutoCommand(private val plugin: PlotPlugin) : CommandBase(
    "auto",
    "souza.plot.command.auto.desc"
) {


    override fun executeSync(context: CommandContext) {
        if (!context.isPlayer) {
            context.sendMessage(Message.raw(plugin.config.messages.playerOnly))
            return
        }

        val player = context.senderAs(Player::class.java)
        val playerRef = player.playerRef

        if (!plugin.plotManager.canPlayerClaim(player.uuid!!)) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.atPlotLimit
            )))
            return
        }

        val plotId = plugin.plotManager.findUnclaimedPlot()
        if (plotId == null) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.noUnclaimedPlots
            )))
            return
        }

        val result = plugin.plotManager.claimPlot(plotId, player.uuid!!)
        if (result != ClaimResult.SUCCESS) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.plotAlreadyClaimed
            )))
            return
        }

        player.saveLastLocationForTpback(plugin.plotManager)

        plugin.worldManager.teleportToPlot(playerRef, plotId)

        context.sendMessage(Message.raw(plugin.config.messages.format(
            plugin.config.messages.autoClaimed, plotId.toString()
        )))
    }
}
