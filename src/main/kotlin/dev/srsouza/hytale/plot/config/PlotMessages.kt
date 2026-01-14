package dev.srsouza.hytale.plot.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

/**
 * Configurable messages for the plot plugin.
 * Use %s or {0}, {1} style placeholders for dynamic values.
 */
class PlotMessages {
    // General
    var prefix: String = "[Plot] "
    var noPermission: String = "You don't have permission to do that."
    var playerNotFound: String = "Player not found: %s"
    var notInPlotWorld: String = "You must be in the plot world to use this command."
    var playerOnly: String = "This command can only be used by players."

    // Claiming
    var plotClaimed: String = "You have claimed plot %s!"
    var plotAlreadyClaimed: String = "This plot is already claimed."
    var atPlotLimit: String = "You have reached your plot limit."
    var cannotClaimRoad: String = "You cannot claim a road. Stand inside a plot to claim it."
    var noUnclaimedPlots: String = "No unclaimed plots found nearby."
    var autoClaimed: String = "You have been assigned plot %s!"

    // Teleportation
    var teleportedToPlot: String = "Teleported to plot %s."
    var teleportedToHome: String = "Teleported to your plot home."
    var teleportedToHomeIndex: String = "Teleported to plot %s of %s."
    var invalidPlotIndex: String = "Invalid plot index: %s. You have %s plots."
    var teleportedBack: String = "Teleported back to your previous location."
    var noPlotOwned: String = "You don't own any plots."
    var plotNotFound: String = "Plot not found."
    var noLastLocation: String = "You don't have a previous location to teleport back to."
    var lastLocationWorldNotFound: String = "The world of your previous location no longer exists."

    // Members
    var memberAdded: String = "Added %s to your plot."
    var memberRemoved: String = "Removed %s from your plot."
    var memberAlreadyAdded: String = "%s is already a member of this plot."
    var memberNotFound: String = "%s is not a member of this plot."
    var cannotAddSelf: String = "You cannot add yourself as a member."

    // Deny
    var playerDenied: String = "Denied %s from your plot."
    var playerUndenied: String = "Removed %s from your deny list."
    var playerAlreadyDenied: String = "%s is already denied from this plot."
    var playerNotDenied: String = "%s is not denied from this plot."
    var youAreDenied: String = "You are denied from entering this plot."

    // Protection
    var cannotBuildHere: String = "You cannot build here."
    var cannotBreakHere: String = "You cannot break blocks here."
    var cannotBreakBottomLayer: String = "You cannot break the bottom layer."
    var cannotInteractHere: String = "You cannot interact here."

    // Plot info
    var plotInfoHeader: String = "=== Plot Info ==="
    var plotInfoId: String = "ID: %s"
    var plotInfoOwner: String = "Owner: %s"
    var plotInfoMembers: String = "Members: %s"
    var plotInfoUnclaimed: String = "This plot is unclaimed."
    var plotInfoAdminPlot: String = "This is a server plot."

    // Admin
    var adminPlotClaimed: String = "Claimed plot %s for the server."
    var adminSpawnSet: String = "Plot world spawn set."
    var adminPlotDeleted: String = "Deleted plot %s."
    var adminPlotReset: String = "Reset plot %s."

    // Merge
    var plotsMerged: String = "Plots merged successfully."
    var cannotMerge: String = "Cannot merge these plots. They must be adjacent and owned by you."
    var mergingDisabled: String = "Plot merging is disabled."

    // Reset
    var plotReset: String = "Your plot has been reset."
    var resetDisabled: String = "Plot resetting is disabled."

    // Entry/Exit
    var enteredPlot: String = "Entering plot owned by %s"
    var leftPlot: String = "Leaving plot"
    var enteredRoad: String = "You are on the road."

    companion object {
        @JvmField
        val CODEC: BuilderCodec<PlotMessages> = BuilderCodec.builder(
            PlotMessages::class.java,
            ::PlotMessages
        )
            // General
            .append(KeyedCodec("Prefix", Codec.STRING),
                { m, v -> m.prefix = v }, { m -> m.prefix }).add()
            .append(KeyedCodec("NoPermission", Codec.STRING),
                { m, v -> m.noPermission = v }, { m -> m.noPermission }).add()
            .append(KeyedCodec("PlayerNotFound", Codec.STRING),
                { m, v -> m.playerNotFound = v }, { m -> m.playerNotFound }).add()
            .append(KeyedCodec("NotInPlotWorld", Codec.STRING),
                { m, v -> m.notInPlotWorld = v }, { m -> m.notInPlotWorld }).add()
            .append(KeyedCodec("PlayerOnly", Codec.STRING),
                { m, v -> m.playerOnly = v }, { m -> m.playerOnly }).add()
            // Claiming
            .append(KeyedCodec("PlotClaimed", Codec.STRING),
                { m, v -> m.plotClaimed = v }, { m -> m.plotClaimed }).add()
            .append(KeyedCodec("PlotAlreadyClaimed", Codec.STRING),
                { m, v -> m.plotAlreadyClaimed = v }, { m -> m.plotAlreadyClaimed }).add()
            .append(KeyedCodec("AtPlotLimit", Codec.STRING),
                { m, v -> m.atPlotLimit = v }, { m -> m.atPlotLimit }).add()
            .append(KeyedCodec("CannotClaimRoad", Codec.STRING),
                { m, v -> m.cannotClaimRoad = v }, { m -> m.cannotClaimRoad }).add()
            .append(KeyedCodec("NoUnclaimedPlots", Codec.STRING),
                { m, v -> m.noUnclaimedPlots = v }, { m -> m.noUnclaimedPlots }).add()
            .append(KeyedCodec("AutoClaimed", Codec.STRING),
                { m, v -> m.autoClaimed = v }, { m -> m.autoClaimed }).add()
            // Teleportation
            .append(KeyedCodec("TeleportedToPlot", Codec.STRING),
                { m, v -> m.teleportedToPlot = v }, { m -> m.teleportedToPlot }).add()
            .append(KeyedCodec("TeleportedToHome", Codec.STRING),
                { m, v -> m.teleportedToHome = v }, { m -> m.teleportedToHome }).add()
            .append(KeyedCodec("TeleportedToHomeIndex", Codec.STRING),
                { m, v -> m.teleportedToHomeIndex = v }, { m -> m.teleportedToHomeIndex }).add()
            .append(KeyedCodec("InvalidPlotIndex", Codec.STRING),
                { m, v -> m.invalidPlotIndex = v }, { m -> m.invalidPlotIndex }).add()
            .append(KeyedCodec("TeleportedBack", Codec.STRING),
                { m, v -> m.teleportedBack = v }, { m -> m.teleportedBack }).add()
            .append(KeyedCodec("NoPlotOwned", Codec.STRING),
                { m, v -> m.noPlotOwned = v }, { m -> m.noPlotOwned }).add()
            .append(KeyedCodec("PlotNotFound", Codec.STRING),
                { m, v -> m.plotNotFound = v }, { m -> m.plotNotFound }).add()
            .append(KeyedCodec("NoLastLocation", Codec.STRING),
                { m, v -> m.noLastLocation = v }, { m -> m.noLastLocation }).add()
            .append(KeyedCodec("LastLocationWorldNotFound", Codec.STRING),
                { m, v -> m.lastLocationWorldNotFound = v }, { m -> m.lastLocationWorldNotFound }).add()
            // Members
            .append(KeyedCodec("MemberAdded", Codec.STRING),
                { m, v -> m.memberAdded = v }, { m -> m.memberAdded }).add()
            .append(KeyedCodec("MemberRemoved", Codec.STRING),
                { m, v -> m.memberRemoved = v }, { m -> m.memberRemoved }).add()
            .append(KeyedCodec("MemberAlreadyAdded", Codec.STRING),
                { m, v -> m.memberAlreadyAdded = v }, { m -> m.memberAlreadyAdded }).add()
            .append(KeyedCodec("MemberNotFound", Codec.STRING),
                { m, v -> m.memberNotFound = v }, { m -> m.memberNotFound }).add()
            .append(KeyedCodec("CannotAddSelf", Codec.STRING),
                { m, v -> m.cannotAddSelf = v }, { m -> m.cannotAddSelf }).add()
            // Deny
            .append(KeyedCodec("PlayerDenied", Codec.STRING),
                { m, v -> m.playerDenied = v }, { m -> m.playerDenied }).add()
            .append(KeyedCodec("PlayerUndenied", Codec.STRING),
                { m, v -> m.playerUndenied = v }, { m -> m.playerUndenied }).add()
            .append(KeyedCodec("PlayerAlreadyDenied", Codec.STRING),
                { m, v -> m.playerAlreadyDenied = v }, { m -> m.playerAlreadyDenied }).add()
            .append(KeyedCodec("PlayerNotDenied", Codec.STRING),
                { m, v -> m.playerNotDenied = v }, { m -> m.playerNotDenied }).add()
            .append(KeyedCodec("YouAreDenied", Codec.STRING),
                { m, v -> m.youAreDenied = v }, { m -> m.youAreDenied }).add()
            // Protection
            .append(KeyedCodec("CannotBuildHere", Codec.STRING),
                { m, v -> m.cannotBuildHere = v }, { m -> m.cannotBuildHere }).add()
            .append(KeyedCodec("CannotBreakHere", Codec.STRING),
                { m, v -> m.cannotBreakHere = v }, { m -> m.cannotBreakHere }).add()
            .append(KeyedCodec("CannotBreakBottomLayer", Codec.STRING),
                { m, v -> m.cannotBreakBottomLayer = v }, { m -> m.cannotBreakBottomLayer }).add()
            .append(KeyedCodec("CannotInteractHere", Codec.STRING),
                { m, v -> m.cannotInteractHere = v }, { m -> m.cannotInteractHere }).add()
            // Plot info
            .append(KeyedCodec("PlotInfoHeader", Codec.STRING),
                { m, v -> m.plotInfoHeader = v }, { m -> m.plotInfoHeader }).add()
            .append(KeyedCodec("PlotInfoId", Codec.STRING),
                { m, v -> m.plotInfoId = v }, { m -> m.plotInfoId }).add()
            .append(KeyedCodec("PlotInfoOwner", Codec.STRING),
                { m, v -> m.plotInfoOwner = v }, { m -> m.plotInfoOwner }).add()
            .append(KeyedCodec("PlotInfoMembers", Codec.STRING),
                { m, v -> m.plotInfoMembers = v }, { m -> m.plotInfoMembers }).add()
            .append(KeyedCodec("PlotInfoUnclaimed", Codec.STRING),
                { m, v -> m.plotInfoUnclaimed = v }, { m -> m.plotInfoUnclaimed }).add()
            .append(KeyedCodec("PlotInfoAdminPlot", Codec.STRING),
                { m, v -> m.plotInfoAdminPlot = v }, { m -> m.plotInfoAdminPlot }).add()
            // Admin
            .append(KeyedCodec("AdminPlotClaimed", Codec.STRING),
                { m, v -> m.adminPlotClaimed = v }, { m -> m.adminPlotClaimed }).add()
            .append(KeyedCodec("AdminSpawnSet", Codec.STRING),
                { m, v -> m.adminSpawnSet = v }, { m -> m.adminSpawnSet }).add()
            .append(KeyedCodec("AdminPlotDeleted", Codec.STRING),
                { m, v -> m.adminPlotDeleted = v }, { m -> m.adminPlotDeleted }).add()
            .append(KeyedCodec("AdminPlotReset", Codec.STRING),
                { m, v -> m.adminPlotReset = v }, { m -> m.adminPlotReset }).add()
            // Merge
            .append(KeyedCodec("PlotsMerged", Codec.STRING),
                { m, v -> m.plotsMerged = v }, { m -> m.plotsMerged }).add()
            .append(KeyedCodec("CannotMerge", Codec.STRING),
                { m, v -> m.cannotMerge = v }, { m -> m.cannotMerge }).add()
            .append(KeyedCodec("MergingDisabled", Codec.STRING),
                { m, v -> m.mergingDisabled = v }, { m -> m.mergingDisabled }).add()
            // Reset
            .append(KeyedCodec("PlotReset", Codec.STRING),
                { m, v -> m.plotReset = v }, { m -> m.plotReset }).add()
            .append(KeyedCodec("ResetDisabled", Codec.STRING),
                { m, v -> m.resetDisabled = v }, { m -> m.resetDisabled }).add()
            // Entry/Exit
            .append(KeyedCodec("EnteredPlot", Codec.STRING),
                { m, v -> m.enteredPlot = v }, { m -> m.enteredPlot }).add()
            .append(KeyedCodec("LeftPlot", Codec.STRING),
                { m, v -> m.leftPlot = v }, { m -> m.leftPlot }).add()
            .append(KeyedCodec("EnteredRoad", Codec.STRING),
                { m, v -> m.enteredRoad = v }, { m -> m.enteredRoad }).add()
            .build()
    }

    /**
     * Helper function to format a message with arguments.
     */
    fun format(message: String, vararg args: Any): String {
        return prefix + String.format(message, *args)
    }
}
