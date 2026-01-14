package dev.srsouza.hytale.plot.worldgen

import dev.srsouza.hytale.plot.PlotPlugin
import dev.srsouza.hytale.plot.model.PlotId
import dev.srsouza.hytale.plot.util.PlotCoordUtil
import com.hypixel.hytale.common.plugin.PluginIdentifier
import com.hypixel.hytale.common.semver.SemverRange
import com.hypixel.hytale.math.vector.Transform
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.WorldConfig
import java.nio.file.Files
import java.util.concurrent.CompletableFuture

/**
 * Manages the plot world creation and player teleportation.
 */
class PlotWorldManager(
    private val plugin: PlotPlugin
) {
    private var plotWorld: World? = null
    private var spawnLocation: Transform? = null

    /**
     * Gets the plot world, or null if not created.
     */
    fun getPlotWorld(): World? = plotWorld

    /**
     * Sets the plot world reference.
     */
    fun setPlotWorld(world: World) {
        this.plotWorld = world
    }

    /**
     * Checks if a world is the plot world.
     */
    fun isPlotWorld(world: World): Boolean {
        return plotWorld != null && world == plotWorld
    }

    /**
     * Gets the spawn location for the plot world.
     */
    fun getSpawnLocation(): Transform {
        return spawnLocation ?: run {
            val config = plugin.config
            val center = PlotCoordUtil.plotIdToWorldCenter(
                PlotId(0, 0),
                config.plotSize,
                config.roadWidth,
                config.groundHeight
            )
            Transform(center.x, center.y, center.z)
        }
    }

    /**
     * Sets the spawn location for the plot world.
     */
    fun setSpawnLocation(location: Transform) {
        this.spawnLocation = location
    }

    /**
     * Creates or loads the plot world.
     */
    fun createPlotWorld(): CompletableFuture<World?> {
        val universe = Universe.get()
        val worldName = plugin.config.worldName

        val existingWorld = universe.getWorld(worldName)
        if (existingWorld != null) {
            plugin.logger.atInfo().log("Plot world '$worldName' already exists")
            plotWorld = existingWorld
            return CompletableFuture.completedFuture(existingWorld)
        }

        if (!plugin.config.autoCreateWorld) {
            plugin.logger.atWarning().log("Plot world '$worldName' does not exist and autoCreateWorld is disabled")
            return CompletableFuture.completedFuture(null)
        }

        plugin.logger.atInfo().log("Creating plot world '$worldName'...")

        return try {
            val universePath = universe.path
            val savePath = universePath.resolve("worlds").resolve(worldName)

            Files.createDirectories(savePath)

            val worldConfig = createPlotWorldConfig()

            universe.makeWorld(worldName, savePath, worldConfig)
                .thenApply { world ->
                    plotWorld = world
                    plugin.logger.atInfo().log("Successfully created plot world '$worldName'")
                    world
                }
                .exceptionally { throwable ->
                    plugin.logger.atSevere().log("Failed to create plot world: ${throwable.message}")
                    null
                }
        } catch (e: Exception) {
            plugin.logger.atSevere().log("Failed to create plot world: ${e.message}")
            CompletableFuture.completedFuture(null)
        }
    }

    /**
     * Creates a WorldConfig configured for the plot world.
     */
    private fun createPlotWorldConfig(): WorldConfig {
        val config = plugin.config
        val worldConfig = WorldConfig()

        // Create and configure the PlotWorldGenProvider
        val plotWorldGenProvider = PlotWorldGenProvider()
        plotWorldGenProvider.plotSize = config.plotSize
        plotWorldGenProvider.roadWidth = config.roadWidth
        plotWorldGenProvider.groundHeight = config.groundHeight
        plotWorldGenProvider.plotBlockType = config.plotBlockType
        plotWorldGenProvider.roadBlockType = config.roadBlockType
        plotWorldGenProvider.bedrockBlockType = config.bedrockBlockType
        plotWorldGenProvider.borderBlockType = config.borderBlockType
        plotWorldGenProvider.fillBlockType = config.fillBlockType
        plotWorldGenProvider.dirtBlockType = config.dirtBlockType
        plotWorldGenProvider.dirtDepth = config.dirtDepth
        plotWorldGenProvider.bedrockDepth = config.bedrockDepth
        plotWorldGenProvider.borderEnabled = config.borderEnabled
        plotWorldGenProvider.environment = config.environment

        worldConfig.setWorldGenProvider(plotWorldGenProvider)

        val requiredPlugins = HashMap<PluginIdentifier, SemverRange>()
        requiredPlugins[PluginIdentifier.fromString("Souza:Plot")] = SemverRange.WILDCARD
        worldConfig.setRequiredPlugins(requiredPlugins)

        worldConfig.setTicking(true)
        worldConfig.setBlockTicking(true)
        worldConfig.setPvpEnabled(false)
        worldConfig.setSpawningNPC(false)

        val spawnCenter = PlotCoordUtil.plotIdToWorldCenter(
            PlotId(0, 0),
            config.plotSize,
            config.roadWidth,
            config.groundHeight
        )
        worldConfig.setSpawnProvider(com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider(
            Transform(spawnCenter.x, spawnCenter.y, spawnCenter.z)
        ))

        worldConfig.markChanged()

        return worldConfig
    }

    /**
     * Teleports a player to a specific plot.
     */
    fun teleportToPlot(player: PlayerRef, plotId: PlotId) {
        val world = plotWorld
        if (world == null) {
            plugin.logger.atWarning().log("Cannot teleport player ${player.username}: Plot world not loaded")
            return
        }

        val config = plugin.config
        val center = PlotCoordUtil.plotIdToWorldCenter(
            plotId,
            config.plotSize,
            config.roadWidth,
            config.groundHeight
        )

        teleportPlayer(player, world, center)
    }

    /**
     * Teleports a player to their plot's home location, or plot center if no home set.
     */
    fun teleportToPlotHome(player: PlayerRef, plotId: PlotId, homeLocation: Vector3d?) {
        val world = plotWorld
        if (world == null) {
            plugin.logger.atWarning().log("Cannot teleport player ${player.username}: Plot world not loaded")
            return
        }

        val config = plugin.config
        val target = homeLocation ?: PlotCoordUtil.plotIdToWorldCenter(
            plotId,
            config.plotSize,
            config.roadWidth,
            config.groundHeight
        )

        teleportPlayer(player, world, target)
    }

    /**
     * Teleports a player to the plot world spawn.
     */
    fun teleportToSpawn(player: PlayerRef) {
        val world = plotWorld
        if (world == null) {
            plugin.logger.atWarning().log("Cannot teleport player ${player.username}: Plot world not loaded")
            return
        }

        val spawn = getSpawnLocation()
        teleportPlayer(player, world, Vector3d(spawn.position.x, spawn.position.y, spawn.position.z))
    }

    /**
     * Gets the current World object from a PlayerRef.
     */
    private fun getWorldFromPlayer(player: PlayerRef): World? {
        val worldUuid = player.worldUuid ?: return null
        return Universe.get().getWorld(worldUuid)
    }

    /**
     * Teleports a player to any world and position with rotation.
     * This is the main teleportation method that should be used by all commands.
     *
     * @param player The player reference to teleport
     * @param targetWorld The target world to teleport to
     * @param position The target position
     * @param rotation The target rotation (pitch, yaw, roll) - defaults to (0, 0, 0)
     */
    fun teleportToLocation(player: PlayerRef, targetWorld: World, position: Vector3d, rotation: Vector3f = Vector3f(0f, 0f, 0f)) {
        teleportPlayer(player, targetWorld, position, rotation)
    }

    /**
     * Internal method to teleport a player to a position.
     * Must execute on the player's current world thread since the component belongs to that store.
     */
    private fun teleportPlayer(player: PlayerRef, targetWorld: World, position: Vector3d, rotation: Vector3f = Vector3f(0f, 0f, 0f)) {
        val ref = player.reference
        if (ref == null) {
            plugin.logger.atWarning().log("Cannot teleport player: not in a world")
            return
        }

        // Get the player's current world - component operations must happen on that thread
        val currentWorld = getWorldFromPlayer(player)
        if (currentWorld == null) {
            plugin.logger.atWarning().log("Cannot teleport player: current world not found")
            return
        }

        // Execute on the player's current world thread since the store belongs to that world
        currentWorld.execute {
            val store = ref.store
            // Use Teleport component with target world - handles both same-world and cross-world teleportation
            val teleport = Teleport(targetWorld, position, rotation)
            store.addComponent(ref, Teleport.getComponentType(), teleport)
        }
    }
}
