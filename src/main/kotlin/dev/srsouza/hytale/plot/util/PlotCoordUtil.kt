package dev.srsouza.hytale.plot.util

import dev.srsouza.hytale.plot.model.PlotId
import com.hypixel.hytale.math.vector.Vector3d
import kotlin.math.floor

/**
 * Utility functions for plot coordinate calculations.
 */
object PlotCoordUtil {

    /**
     * Gets the minimum corner (bottom-left) of a plot in world coordinates.
     *
     * @param plotId The plot ID
     * @param plotSize Size of each plot
     * @param roadWidth Width of roads between plots
     * @return Pair of (worldX, worldZ) for the minimum corner
     */
    fun plotIdToWorldMin(plotId: PlotId, plotSize: Int, roadWidth: Int): Pair<Int, Int> {
        val gridSize = plotSize + roadWidth
        val worldX = plotId.x * gridSize + roadWidth
        val worldZ = plotId.z * gridSize + roadWidth
        return Pair(worldX, worldZ)
    }

    /**
     * Gets the maximum corner (top-right) of a plot in world coordinates.
     *
     * @param plotId The plot ID
     * @param plotSize Size of each plot
     * @param roadWidth Width of roads between plots
     * @return Pair of (worldX, worldZ) for the maximum corner (inclusive)
     */
    fun plotIdToWorldMax(plotId: PlotId, plotSize: Int, roadWidth: Int): Pair<Int, Int> {
        val (minX, minZ) = plotIdToWorldMin(plotId, plotSize, roadWidth)
        return Pair(minX + plotSize - 1, minZ + plotSize - 1)
    }

    /**
     * Gets the center of a plot in world coordinates.
     *
     * @param plotId The plot ID
     * @param plotSize Size of each plot
     * @param roadWidth Width of roads between plots
     * @param groundHeight The Y level of the ground
     * @return Vector3d at the center of the plot, one block above ground
     */
    fun plotIdToWorldCenter(plotId: PlotId, plotSize: Int, roadWidth: Int, groundHeight: Int): Vector3d {
        val (minX, minZ) = plotIdToWorldMin(plotId, plotSize, roadWidth)
        val centerX = minX + plotSize / 2.0
        val centerZ = minZ + plotSize / 2.0
        return Vector3d(centerX, (groundHeight + 1).toDouble(), centerZ)
    }

    /**
     * Checks if a world position is within a plot's bounds.
     *
     * @param worldX World X coordinate
     * @param worldZ World Z coordinate
     * @param plotId The plot ID to check
     * @param plotSize Size of each plot
     * @param roadWidth Width of roads between plots
     * @return true if the position is within the plot
     */
    fun isInPlot(worldX: Int, worldZ: Int, plotId: PlotId, plotSize: Int, roadWidth: Int): Boolean {
        val (minX, minZ) = plotIdToWorldMin(plotId, plotSize, roadWidth)
        return worldX >= minX && worldX < minX + plotSize &&
               worldZ >= minZ && worldZ < minZ + plotSize
    }

    /**
     * Gets all adjacent plot IDs (north, south, east, west).
     *
     * @param plotId The center plot ID
     * @return List of 4 adjacent PlotIds
     */
    fun getAdjacentPlots(plotId: PlotId): List<PlotId> {
        return listOf(
            PlotId(plotId.x - 1, plotId.z),  // West
            PlotId(plotId.x + 1, plotId.z),  // East
            PlotId(plotId.x, plotId.z - 1),  // North
            PlotId(plotId.x, plotId.z + 1)   // South
        )
    }

    /**
     * Gets all 8 surrounding plot IDs (including diagonals).
     *
     * @param plotId The center plot ID
     * @return List of 8 surrounding PlotIds
     */
    fun getSurroundingPlots(plotId: PlotId): List<PlotId> {
        val plots = mutableListOf<PlotId>()
        for (dx in -1..1) {
            for (dz in -1..1) {
                if (dx != 0 || dz != 0) {
                    plots.add(PlotId(plotId.x + dx, plotId.z + dz))
                }
            }
        }
        return plots
    }

    /**
     * Calculates the distance (in plots) from the origin.
     * Uses Chebyshev distance (max of absolute x and z).
     *
     * @param plotId The plot ID
     * @return Distance from origin in plot units
     */
    fun distanceFromOrigin(plotId: PlotId): Int {
        return maxOf(kotlin.math.abs(plotId.x), kotlin.math.abs(plotId.z))
    }

    /**
     * Generates plot IDs in a spiral pattern from the origin.
     * Useful for finding unclaimed plots efficiently.
     *
     * @param maxDistance Maximum distance from origin to search
     * @return Sequence of PlotIds in spiral order
     */
    fun spiralFromOrigin(maxDistance: Int): Sequence<PlotId> = sequence {
        yield(PlotId(0, 0))

        var x = 0
        var z = 0
        var direction = 0  // 0=right, 1=down, 2=left, 3=up
        var stepsInDirection = 1
        var stepsTaken = 0
        var turnsAtCurrentStep = 0

        val totalPlots = (2 * maxDistance + 1) * (2 * maxDistance + 1)
        var plotsYielded = 1

        while (plotsYielded < totalPlots) {
            // Move in current direction
            when (direction) {
                0 -> x++  // right
                1 -> z++  // down
                2 -> x--  // left
                3 -> z--  // up
            }
            stepsTaken++

            if (kotlin.math.abs(x) <= maxDistance && kotlin.math.abs(z) <= maxDistance) {
                yield(PlotId(x, z))
                plotsYielded++
            }

            // Check if we need to turn
            if (stepsTaken >= stepsInDirection) {
                stepsTaken = 0
                direction = (direction + 1) % 4
                turnsAtCurrentStep++

                // After two turns, increase steps in direction
                if (turnsAtCurrentStep == 2) {
                    turnsAtCurrentStep = 0
                    stepsInDirection++
                }
            }
        }
    }

    /**
     * Gets the chunk indices that contain a plot.
     *
     * @param plotId The plot ID
     * @param plotSize Size of each plot
     * @param roadWidth Width of roads between plots
     * @return Set of chunk indices (as Long values)
     */
    fun getChunksForPlot(plotId: PlotId, plotSize: Int, roadWidth: Int): Set<Long> {
        val (minX, minZ) = plotIdToWorldMin(plotId, plotSize, roadWidth)
        val (maxX, maxZ) = plotIdToWorldMax(plotId, plotSize, roadWidth)

        val chunks = mutableSetOf<Long>()
        val minChunkX = floor(minX.toDouble() / 32).toInt()
        val maxChunkX = floor(maxX.toDouble() / 32).toInt()
        val minChunkZ = floor(minZ.toDouble() / 32).toInt()
        val maxChunkZ = floor(maxZ.toDouble() / 32).toInt()

        for (cx in minChunkX..maxChunkX) {
            for (cz in minChunkZ..maxChunkZ) {
                // Combine chunk coordinates into a single long index
                chunks.add(chunkIndex(cx, cz))
            }
        }
        return chunks
    }

    /**
     * Converts chunk X and Z to a chunk index.
     */
    private fun chunkIndex(cx: Int, cz: Int): Long {
        return (cx.toLong() and 0xFFFFFFFFL) or ((cz.toLong() and 0xFFFFFFFFL) shl 32)
    }
}
