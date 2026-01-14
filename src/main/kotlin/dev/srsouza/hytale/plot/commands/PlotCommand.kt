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
    "souza.plot.command.plot.desc"
) {

}
