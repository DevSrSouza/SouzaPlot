package dev.srsouza.hytale.plot.model

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import kotlin.math.floor

/**
 * Represents a plot's grid coordinates (not world coordinates).
 * Plot (0,0) is at the origin, Plot (1,0) is one plot east, etc.
 */
data class PlotId(val x: Int, val z: Int) : Comparable<PlotId> {

    override fun compareTo(other: PlotId): Int {
        val xCompare = x.compareTo(other.x)
        return if (xCompare != 0) xCompare else z.compareTo(other.z)
    }

    override fun toString(): String = "$x;$z"

    companion object {
        @JvmField
        val CODEC: BuilderCodec<PlotId> = BuilderCodec.builder(PlotId::class.java) { PlotId(0, 0) }
            .append(
                KeyedCodec("X", Codec.INTEGER),
                { plotId, value -> PlotId(value, plotId.z) },
                { plotId -> plotId.x }
            ).add()
            .append(
                KeyedCodec("Z", Codec.INTEGER),
                { plotId, value -> PlotId(plotId.x, value) },
                { plotId -> plotId.z }
            ).add()
            .build()

        /**
         * Parses a PlotId from a string in the format "x;z".
         */
        fun parse(str: String): PlotId? {
            val parts = str.split(";")
            if (parts.size != 2) return null
            val x = parts[0].toIntOrNull() ?: return null
            val z = parts[1].toIntOrNull() ?: return null
            return PlotId(x, z)
        }

        /**
         * Converts world coordinates to a PlotId.
         *
         * @param worldX World X coordinate
         * @param worldZ World Z coordinate
         * @param plotSize Size of each plot (e.g., 32)
         * @param roadWidth Width of roads between plots (e.g., 5)
         * @return PlotId or null if position is on a road
         */
        fun fromWorldCoords(worldX: Int, worldZ: Int, plotSize: Int, roadWidth: Int): PlotId? {
            val gridSize = plotSize + roadWidth

            // Grid cell index (which grid cell we're in)
            val cellX = floor(worldX.toDouble() / gridSize).toInt()
            val cellZ = floor(worldZ.toDouble() / gridSize).toInt()

            // Position within the grid cell (0 to gridSize-1)
            val localX = Math.floorMod(worldX, gridSize)
            val localZ = Math.floorMod(worldZ, gridSize)

            // Check if on road (road is at the start of each grid cell)
            if (localX < roadWidth || localZ < roadWidth) {
                return null  // On road
            }

            return PlotId(cellX, cellZ)
        }

        /**
         * Checks if the given world coordinates are on a road.
         */
        fun isOnRoad(worldX: Int, worldZ: Int, plotSize: Int, roadWidth: Int): Boolean {
            val gridSize = plotSize + roadWidth
            val localX = Math.floorMod(worldX, gridSize)
            val localZ = Math.floorMod(worldZ, gridSize)
            return localX < roadWidth || localZ < roadWidth
        }

        /**
         * Checks if the given world coordinates are on a plot border.
         */
        fun isOnBorder(worldX: Int, worldZ: Int, plotSize: Int, roadWidth: Int): Boolean {
            val gridSize = plotSize + roadWidth
            val localX = Math.floorMod(worldX, gridSize)
            val localZ = Math.floorMod(worldZ, gridSize)

            // Not on road
            if (localX < roadWidth || localZ < roadWidth) {
                return false
            }

            // Border is the first and last block of the plot area
            val plotLocalX = localX - roadWidth
            val plotLocalZ = localZ - roadWidth
            return plotLocalX == 0 || plotLocalX == plotSize - 1 ||
                   plotLocalZ == 0 || plotLocalZ == plotSize - 1
        }
    }
}
