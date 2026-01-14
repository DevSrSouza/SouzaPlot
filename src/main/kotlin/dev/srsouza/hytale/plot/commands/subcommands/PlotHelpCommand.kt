package dev.srsouza.hytale.plot.commands.subcommands

import dev.srsouza.hytale.plot.PlotPlugin
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase

/**
 * /plot help - Shows help for plot commands.
 */
class PlotHelpCommand(private val plugin: PlotPlugin) : CommandBase(
    "help",
    "souza.plot.command.help.desc"
) {


    override fun executeSync(context: CommandContext) {
        context.sendMessage(Message.raw("=== Plot Commands ==="))
        context.sendMessage(Message.raw("/plot claim     - Claim the plot you're standing in"))
        context.sendMessage(Message.raw("/plot auto      - Automatically claim a random plot"))
        context.sendMessage(Message.raw("/plot home      - Teleport to your plot"))
        context.sendMessage(Message.raw("/plot visit <player> - Visit another player's plot"))
        context.sendMessage(Message.raw("/plot info      - Show info about current plot"))
        context.sendMessage(Message.raw("/plot list      - List your claimed plots"))
        context.sendMessage(Message.raw("/plot add <player> - Add a member to your plot"))
        context.sendMessage(Message.raw("/plot remove <player> - Remove a member from your plot"))
        context.sendMessage(Message.raw("/plot deny <player> - Deny a player from your plot"))
        context.sendMessage(Message.raw("/plot undeny <player> - Allow a denied player again"))
    }
}
