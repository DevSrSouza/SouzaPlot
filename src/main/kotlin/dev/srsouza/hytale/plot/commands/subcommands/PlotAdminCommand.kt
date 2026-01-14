package dev.srsouza.hytale.plot.commands.subcommands

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.manager.ClaimResult
import dev.srsouza.hytale.plot.model.PlotId
import com.hypixel.hytale.math.vector.Transform
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.permissions.PermissionsModule

/**
 * /plot admin - Admin commands for plot management.
 */
class PlotAdminCommand(private val plugin: PlotPlugin) : AbstractCommandCollection(
    "admin",
    "souza.plot.command.admin.desc"
) {


    /**
     * /plot admin claim - Claim a plot for the server.
     */
    private class AdminClaimCommand(private val plugin: PlotPlugin) : CommandBase(
        "claim",
        "souza.plot.command.admin.claim.desc"
    ) {
        override fun executeSync(context: CommandContext) {
            if (!context.isPlayer) {
                context.sendMessage(Message.raw(plugin.config.messages.playerOnly))
                return
            }

            val player = context.senderAs(Player::class.java)

            if (!PermissionsModule.get().hasPermission(player.uuid!!, "souza.plot.admin")) {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.noPermission
                )))
                return
            }

            val world = player.world
            if (world == null || !plugin.worldManager.isPlotWorld(world)) {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.notInPlotWorld
                )))
                return
            }

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

            val result = plugin.plotManager.claimAdminPlot(plotId)
            when (result) {
                ClaimResult.SUCCESS -> {
                    context.sendMessage(Message.raw(plugin.config.messages.format(
                        plugin.config.messages.adminPlotClaimed, plotId.toString()
                    )))
                }
                ClaimResult.ALREADY_CLAIMED -> {
                    context.sendMessage(Message.raw(plugin.config.messages.format(
                        plugin.config.messages.plotAlreadyClaimed
                    )))
                }
                else -> {
                    context.sendMessage(Message.raw("Failed to claim admin plot"))
                }
            }
        }
    }

    /**
     * /plot admin setspawn - Set the plot world spawn location.
     */
    private class AdminSetSpawnCommand(private val plugin: PlotPlugin) : CommandBase(
        "setspawn",
        "souza.plot.command.admin.setspawn.desc"
    ) {
        override fun executeSync(context: CommandContext) {
            if (!context.isPlayer) {
                context.sendMessage(Message.raw(plugin.config.messages.playerOnly))
                return
            }

            val player = context.senderAs(Player::class.java)

            if (!PermissionsModule.get().hasPermission(player.uuid!!, "souza.plot.admin")) {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.noPermission
                )))
                return
            }

            val world = player.world
            if (world == null || !plugin.worldManager.isPlotWorld(world)) {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.notInPlotWorld
                )))
                return
            }

            val transform = player.playerRef.transform
            val spawn = Transform(transform.position.x, transform.position.y, transform.position.z,
                transform.rotation.x, transform.rotation.y, transform.rotation.z)

            plugin.worldManager.setSpawnLocation(spawn)

            context.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.adminSpawnSet
            )))
        }
    }

    /**
     * /plot admin delete - Delete a plot.
     */
    private class AdminDeleteCommand(private val plugin: PlotPlugin) : CommandBase(
        "delete",
        "souza.plot.command.admin.delete.desc"
    ) {
        override fun executeSync(context: CommandContext) {
            if (!context.isPlayer) {
                context.sendMessage(Message.raw(plugin.config.messages.playerOnly))
                return
            }

            val player = context.senderAs(Player::class.java)

            if (!PermissionsModule.get().hasPermission(player.uuid!!, "souza.plot.admin")) {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.noPermission
                )))
                return
            }

            val world = player.world
            if (world == null || !plugin.worldManager.isPlotWorld(world)) {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.notInPlotWorld
                )))
                return
            }

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

            if (plugin.plotManager.unclaimPlot(plotId)) {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.adminPlotDeleted, plotId.toString()
                )))
            } else {
                context.sendMessage(Message.raw(plugin.config.messages.format(
                    plugin.config.messages.plotNotFound
                )))
            }
        }
    }

    /**
     * /plot admin info - Show admin info about the plugin.
     */
    private class AdminInfoCommand(private val plugin: PlotPlugin) : CommandBase(
        "info",
        "souza.plot.command.admin.info.desc"
    ) {
        override fun executeSync(context: CommandContext) {
            if (context.isPlayer) {
                val player = context.senderAs(Player::class.java)
                if (!PermissionsModule.get().hasPermission(player.uuid!!, "souza.plot.admin")) {
                    context.sendMessage(Message.raw(plugin.config.messages.format(
                        plugin.config.messages.noPermission
                    )))
                    return
                }
            }

            context.sendMessage(Message.raw("=== Hytale Plot Admin Info ==="))
            context.sendMessage(Message.raw("Total plots: ${plugin.plotManager.getTotalPlotCount()}"))
            context.sendMessage(Message.raw("Admin plots: ${plugin.plotManager.getAdminPlots().size}"))
            context.sendMessage(Message.raw("Plot size: ${plugin.config.plotSize}x${plugin.config.plotSize}"))
            context.sendMessage(Message.raw("Road width: ${plugin.config.roadWidth}"))
            context.sendMessage(Message.raw("Default plot limit: ${plugin.config.defaultPlotLimit}"))
        }
    }
}
