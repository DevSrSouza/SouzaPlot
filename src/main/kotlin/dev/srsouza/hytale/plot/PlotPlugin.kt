package dev.srsouza.hytale.plot

import dev.srsouza.hytale.plot.commands.PlotCommand
import dev.srsouza.hytale.plot.config.PlotConfig
import dev.srsouza.hytale.plot.database.DatabaseManager
import dev.srsouza.hytale.plot.database.PlayerLastLocationRepository
import dev.srsouza.hytale.plot.database.PlotRepository
import dev.srsouza.hytale.plot.listener.BlockBreakProtectionSystem
import dev.srsouza.hytale.plot.listener.BlockPlaceProtectionSystem
import dev.srsouza.hytale.plot.listener.PlayerListener
import dev.srsouza.hytale.plot.manager.PlotManager
import dev.srsouza.hytale.plot.manager.PlotProtectionManager
import dev.srsouza.hytale.plot.worldgen.PlotWorldGenProvider
import dev.srsouza.hytale.plot.worldgen.PlotWorldManager
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider
import com.hypixel.hytale.server.core.util.Config
import java.util.logging.Level
import javax.annotation.Nonnull

/**
 * Main plugin class for Hytale Plot.
 * Provides PlotMe/PlotSquared-like functionality for Hytale servers.
 */
class PlotPlugin(@Nonnull init: JavaPluginInit) : JavaPlugin(init) {

    // Configuration
    private val _config: Config<PlotConfig> = withConfig(PlotConfig.CODEC)
    lateinit var config: PlotConfig
        private set

    // Database
    private lateinit var databaseManager: DatabaseManager
    private lateinit var plotRepository: PlotRepository
    private lateinit var lastLocationRepository: PlayerLastLocationRepository

    // Managers
    lateinit var plotManager: PlotManager
        private set
    lateinit var worldManager: PlotWorldManager
        private set
    lateinit var protectionManager: PlotProtectionManager
        private set

    // Listeners
    private lateinit var playerListener: PlayerListener

    /**
     * Setup phase - called when the plugin is loaded.
     * Initialize all components here.
     */
    override fun setup() {
        logger.atInfo().log("Setting up Hytale Plot plugin...")

        try {
            config = _config.get()
            // Save config to disk to generate default config file for users to edit
            _config.save()
            logger.atInfo().log("Configuration loaded: plotSize=%d, roadWidth=%d", config.plotSize, config.roadWidth)

            registerWorldGenProvider()

            databaseManager = DatabaseManager(config.database, dataDirectory)
            databaseManager.initialize()
            plotRepository = PlotRepository(databaseManager.getDatabase())
            lastLocationRepository = PlayerLastLocationRepository(databaseManager.getDatabase())
            logger.atInfo().log("Database initialized")

            worldManager = PlotWorldManager(this)
            plotManager = PlotManager(this, plotRepository, lastLocationRepository)
            protectionManager = PlotProtectionManager(this)
            logger.atInfo().log("Managers initialized")

            playerListener = PlayerListener(this)

            val entityStoreRegistry = entityStoreRegistry
            entityStoreRegistry.registerSystem(BlockBreakProtectionSystem(this))
            entityStoreRegistry.registerSystem(BlockPlaceProtectionSystem(this))
            logger.atInfo().log("Block protection systems registered")

            playerListener.register(eventRegistry)
            logger.atInfo().log("Event listeners registered")

            commandRegistry.registerCommand(PlotCommand(this))
            logger.atInfo().log("Commands registered")

            logger.atInfo().log("Hytale Plot plugin setup complete")
        } catch (e: Exception) {
            logger.at(Level.SEVERE).withCause(e).log("Failed to setup Hytale Plot plugin")
            throw e
        }
    }

    /**
     * Start phase - called when the server is starting.
     * Load data and start services here.
     */
    override fun start() {
        logger.atInfo().log("Starting Hytale Plot plugin...")

        try {
            worldManager.createPlotWorld().thenAccept { world ->
                if (world != null) {
                    logger.atInfo().log("Plot world loaded: %s", config.worldName)
                } else {
                    logger.atWarning().log("Plot world not available. Create it manually or enable autoCreateWorld.")
                }
            }

            plotManager.loadAllPlots()

            logger.atInfo().log("Hytale Plot plugin started successfully")
            logger.atInfo().log("Total plots: %d", plotManager.getTotalPlotCount())
        } catch (e: Exception) {
            logger.at(Level.SEVERE).withCause(e).log("Failed to start Hytale Plot plugin")
        }
    }

    /**
     * Shutdown phase - called when the server is stopping.
     * Clean up resources here.
     */
    override fun shutdown() {
        logger.atInfo().log("Shutting down Hytale Plot plugin...")

        try {
            if (::databaseManager.isInitialized) {
                databaseManager.shutdown()
            }

            logger.atInfo().log("Hytale Plot plugin shut down")
        } catch (e: Exception) {
            logger.at(Level.SEVERE).withCause(e).log("Error during Hytale Plot plugin shutdown")
        }
    }

    /**
     * Registers the plot world generator with Hytale's codec system.
     */
    private fun registerWorldGenProvider() {
        try {
            // Register the Plot world generator type
            // This allows creating worlds with WorldGen Type: "Plot"
            IWorldGenProvider.CODEC.register(
                PlotWorldGenProvider.ID,
                PlotWorldGenProvider::class.java,
                PlotWorldGenProvider.CODEC
            )
            logger.atInfo().log("Registered Plot world generator")
        } catch (e: Exception) {
            logger.at(Level.SEVERE).withCause(e).log("Failed to register Plot world generator: %s", e.message)
        }
    }

    /**
     * Reloads the plugin configuration.
     */
    fun reloadConfiguration() {
        logger.atInfo().log("Reloading configuration...")
        config = _config.get()
        logger.atInfo().log("Configuration reloaded")
    }
}
