package dev.srsouza.hytale.plot.commands

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.commands.subcommands.*
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

/**
 * Main /plot command collection.
 * Contains all plot-related subcommands.
 */
class PlotCommand(plugin: PlotPlugin) : AbstractCommandCollection(
    "plot",
    "Plot management commands"
) {
    init {
        requirePermission("souza.plot.command.plot")
        addSubCommand(PlotClaimCommand(plugin))
        addSubCommand(PlotAutoCommand(plugin))
        addSubCommand(PlotHomeCommand(plugin))
        addSubCommand(PlotVisitCommand(plugin))
        addSubCommand(PlotInfoCommand(plugin))
        addSubCommand(PlotListCommand(plugin))
        addSubCommand(PlotAddCommand(plugin))
        addSubCommand(PlotRemoveCommand(plugin))
        addSubCommand(PlotDenyCommand(plugin))
        addSubCommand(PlotUndenyCommand(plugin))
        addSubCommand(PlotTpbackCommand(plugin))
        addSubCommand(PlotHelpCommand(plugin))
        addSubCommand(PlotAdminCommand(plugin))
    }
}
