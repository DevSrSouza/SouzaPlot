package dev.srsouza.hytale.plot.commands.subcommands

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.manager.ClaimResult
import dev.srsouza.hytale.plot.model.PlotId
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player

/**
 * /plot claim - Claim the plot the player is standing in.
 */
class PlotClaimCommand(private val plugin: PlotPlugin) : CommandBase(
    "claim",
    "souza.plot.command.claim.desc"
) {


    override fun executeSync(context: CommandContext) {
        if (!context.isPlayer) {
            context.sendMessage(Message.raw(plugin.config.messages.playerOnly))
            return
        }

        val player = context.senderAs(Player::class.java)
        val world = player.world

        if (world == null || !plugin.worldManager.isPlotWorld(world)) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.notInPlotWorld
            )))
            return
        }

        val position = player.playerRef.transform.position
        val worldX = position.x.toInt()
        val worldZ = position.z.toInt()

        val config = plugin.config
        val plotId = PlotId.fromWorldCoords(worldX, worldZ, config.plotSize, config.roadWidth)

        if (plotId == null) {
            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.cannotClaimRoad
            )))
            return
        }

        val result = plugin.plotManager.claimPlot(plotId, player.uuid!!)

        when (result) {
            ClaimResult.SUCCESS -> {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.plotClaimed, plotId.toString()
                )))
            }
            ClaimResult.ALREADY_CLAIMED -> {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.plotAlreadyClaimed
                )))
            }
            ClaimResult.AT_LIMIT -> {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.atPlotLimit
                )))
            }
            ClaimResult.NO_PERMISSION -> {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.noPermission
                )))
            }
            ClaimResult.ON_ROAD -> {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.cannotClaimRoad
                )))
            }
        }
    }
}
