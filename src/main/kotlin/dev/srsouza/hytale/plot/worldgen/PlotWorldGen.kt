package dev.srsouza.hytale.plot.worldgen

import com.hypixel.hytale.math.vector.Transform
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockStateChunk
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedEntityChunk
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenTimingsCollector
import java.util.concurrent.CompletableFuture
import java.util.function.LongPredicate
import javax.annotation.Nonnull
import javax.annotation.Nullable

/**
 * World generator that creates a flat plot world with roads and borders.
 *
 * Layout (example with plotSize=32, roadWidth=5):
 * ```
 * +-----+--------------------------------+-----+--------------------------------+
 * |ROAD |            ROAD                |ROAD |            ROAD                |
 * +-----+---+------------------------+---+-----+---+------------------------+---+
 * |     | B |                        | B |     | B |                        | B |
 * |ROAD | O |         PLOT           | O |ROAD | O |         PLOT           | O |
 * |     | R |        (0,0)           | R |     | R |        (1,0)           | R |
 * |     | D |                        | D |     | D |                        | D |
 * |     | E |                        | E |     | E |                        | E |
 * |     | R |                        | R |     | R |                        | R |
 * +-----+---+------------------------+---+-----+---+------------------------+---+
 * ```
 *
 * Layer structure (with groundHeight=64):
 * ```
 * Y=65+  Air (empty)
 * Y=65   Border wall (slabs, only at plot edges)
 * Y=64   Surface: Grass (plots) / Cobble (roads)
 * Y=60-63 Subsurface: Dirt (plots) / Stone (roads)
 * Y=3-59  Fill: Stone (everywhere)
 * Y=0-2   Bedrock (everywhere)
 * ```
 */
class PlotWorldGen(
    private val plotSize: Int,
    private val roadWidth: Int,
    private val groundHeight: Int,
    private val plotBlockId: Int,
    private val roadBlockId: Int,
    private val bedrockBlockId: Int,
    private val borderBlockId: Int,
    private val fillBlockId: Int,
    private val dirtBlockId: Int,
    private val bedrockDepth: Int,
    private val dirtDepth: Int,
    private val borderEnabled: Boolean,
    private val environmentId: Int,
    private val tintId: Int
) : IWorldGen {

    private val gridSize = plotSize + roadWidth

    /**
     * Position types within the plot grid.
     */
    private enum class PositionType {
        PLOT,        // Inside buildable plot area
        PLOT_BORDER, // Edge of plot (border block)
        ROAD         // Road between plots
    }

    @Nullable
    override fun getTimings(): WorldGenTimingsCollector? = null

    @Nonnull
    override fun getSpawnPoints(seed: Int): Array<Transform> {
        // Spawn at the center of plot (0,0), one block above the surface
        val spawnX = roadWidth + plotSize / 2.0
        val spawnY = groundHeight + 1.5  // Surface is at groundHeight, spawn above it
        val spawnZ = roadWidth + plotSize / 2.0
        return arrayOf(Transform(spawnX, spawnY, spawnZ))
    }

    @Nonnull
    override fun generate(
        seed: Int,
        index: Long,
        cx: Int,
        cz: Int,
        stillNeeded: LongPredicate?
    ): CompletableFuture<GeneratedChunk> {
        val blockChunk = GeneratedBlockChunk(index, cx, cz)

        // World coordinate offset for this chunk
        val worldOffsetX = cx * 32
        val worldOffsetZ = cz * 32

        // Generate each block column in the 32x32 chunk
        for (localX in 0 until 32) {
            for (localZ in 0 until 32) {
                val worldX = worldOffsetX + localX
                val worldZ = worldOffsetZ + localZ

                // Set tint for this column
                blockChunk.setTint(localX, localZ, tintId)

                // Determine position type (plot, border, or road)
                val posType = getPositionType(worldX, worldZ)

                // Generate the column based on position type
                generateColumn(blockChunk, localX, localZ, posType)
            }
        }

        return CompletableFuture.completedFuture(
            GeneratedChunk(
                blockChunk,
                GeneratedBlockStateChunk(),
                GeneratedEntityChunk(),
                GeneratedChunk.makeSections()
            )
        )
    }

    /**
     * Generates a single column of blocks with proper layer structure.
     */
    private fun generateColumn(
        chunk: GeneratedBlockChunk,
        localX: Int,
        localZ: Int,
        posType: PositionType
    ) {
        // Determine surface block based on position type
        val surfaceBlockId = when (posType) {
            PositionType.PLOT -> plotBlockId
            PositionType.PLOT_BORDER -> if (borderEnabled) borderBlockId else plotBlockId
            PositionType.ROAD -> roadBlockId
        }

        // Generate layers from bottom to top (0 to groundHeight inclusive)
        for (y in 0..groundHeight) {
            val blockId = when {
                // Bedrock layer (bottom)
                y < bedrockDepth -> bedrockBlockId

                // Stone/fill layer (middle)
                y < groundHeight - dirtDepth -> fillBlockId

                // Subsurface layer (dirt for plots, stone for roads)
                y < groundHeight -> {
                    if (posType == PositionType.ROAD) fillBlockId else dirtBlockId
                }

                // Surface layer (y == groundHeight)
                else -> surfaceBlockId
            }

            chunk.setBlock(localX, y, localZ, blockId, 0, 0)
            chunk.setEnvironment(localX, y, localZ, environmentId)
        }

        // Add border wall if on plot border and border is enabled
        if (posType == PositionType.PLOT_BORDER && borderEnabled) {
            chunk.setBlock(localX, groundHeight + 1, localZ, borderBlockId, 0, 0)
            chunk.setEnvironment(localX, groundHeight + 1, localZ, environmentId)
        }
    }

    /**
     * Determines what type of position this is in the grid.
     */
    private fun getPositionType(worldX: Int, worldZ: Int): PositionType {
        // Handle negative coordinates correctly with floorMod
        val localX = Math.floorMod(worldX, gridSize)
        val localZ = Math.floorMod(worldZ, gridSize)

        // Check if on road (road is at the start of each grid cell)
        if (localX < roadWidth || localZ < roadWidth) {
            return PositionType.ROAD
        }

        // Position within the plot area (0 to plotSize-1)
        val plotLocalX = localX - roadWidth
        val plotLocalZ = localZ - roadWidth

        // Check if on plot border (edge of plot)
        if (plotLocalX == 0 || plotLocalX == plotSize - 1 ||
            plotLocalZ == 0 || plotLocalZ == plotSize - 1) {
            return PositionType.PLOT_BORDER
        }

        // Inside the plot
        return PositionType.PLOT
    }

    /**
     * Checks if the given world coordinates are on a road.
     */
    fun isRoad(worldX: Int, worldZ: Int): Boolean {
        return getPositionType(worldX, worldZ) == PositionType.ROAD
    }

    /**
     * Checks if the given world coordinates are on a plot border.
     */
    fun isBorder(worldX: Int, worldZ: Int): Boolean {
        return getPositionType(worldX, worldZ) == PositionType.PLOT_BORDER
    }

    /**
     * Checks if the given world coordinates are inside a plot (including border).
     */
    fun isInPlot(worldX: Int, worldZ: Int): Boolean {
        val posType = getPositionType(worldX, worldZ)
        return posType == PositionType.PLOT || posType == PositionType.PLOT_BORDER
    }
}
