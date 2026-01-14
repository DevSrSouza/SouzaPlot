package dev.srsouza.hytale.plot.commands.subcommands

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.model.PlotId
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.Universe

/**
 * /plot info - Show info about the current plot.
 */
class PlotInfoCommand(private val plugin: PlotPlugin) : CommandBase(
    "info",
    "souza.plot.command.info.desc"
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
                plugin.config.messages.enteredRoad
            )))
            return
        }

        val plot = plugin.plotManager.getPlot(plotId)

        context.sendMessage(Message.raw(plugin.config.messages.plotInfoHeader))
        context.sendMessage(Message.raw(String.format(plugin.config.messages.plotInfoId, plotId.toString())))

        if (plot == null) {
            context.sendMessage(Message.raw(plugin.config.messages.plotInfoUnclaimed))
        } else if (plot.isAdminPlot()) {
            context.sendMessage(Message.raw(plugin.config.messages.plotInfoAdminPlot))
        } else {
            val ownerName = plot.owner?.let { uuid ->
                Universe.get().players.find { it.uuid == uuid }?.username ?: uuid.toString()
            } ?: "Unknown"

            context.sendMessage(Message.raw(String.format(plugin.config.messages.plotInfoOwner, ownerName)))

            if (plot.members.isNotEmpty()) {
                val memberNames = plot.members.mapNotNull { member ->
                    Universe.get().players.find { it.uuid == member.uuid }?.username
                        ?: member.uuid.toString().substring(0, 8)
                }.joinToString(", ")
                context.sendMessage(Message.raw(String.format(plugin.config.messages.plotInfoMembers, memberNames)))
            }

            plot.alias?.let { alias ->
                context.sendMessage(Message.raw("Alias: $alias"))
            }
        }
    }
}
