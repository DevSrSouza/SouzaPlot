package dev.srsouza.hytale.plot.model

/**
 * Per-plot configurable settings/flags.
 */
data class PlotSettings(
    /** Whether PvP is enabled on this plot */
    var pvp: Boolean = false,
    /** Whether mobs can spawn on this plot */
    var mobSpawning: Boolean = false,
    /** Whether weather effects are visible on this plot */
    var weather: Boolean = true,
    /** Fixed time of day for this plot (null = server time) */
    var fixedTime: Int? = null,
    /** Custom entry message when entering the plot */
    var entryMessage: String? = null,
    /** Custom exit message when leaving the plot */
    var exitMessage: String? = null,
    /** Override game mode for players on this plot (null = server default) */
    var gameMode: String? = null
) {
    companion object {
        fun default(): PlotSettings = PlotSettings()
    }
}
