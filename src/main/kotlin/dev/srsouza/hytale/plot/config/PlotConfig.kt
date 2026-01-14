package dev.srsouza.hytale.plot.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

/**
 * Database configuration for the plot plugin.
 */
class DatabaseConfig {
    var jdbcUrl: String = "jdbc:h2:file:./plugins/Hytale/Plot/data/plots;MODE=MySQL"
    var driverClassName: String = "org.h2.Driver"
    var username: String = ""
    var password: String = ""
    var maximumPoolSize: Int = 10
    var minimumIdle: Int = 2
    var idleTimeout: Long = 600000
    var connectionTimeout: Long = 30000
    var maxLifetime: Long = 1800000

    companion object {
        @JvmField
        val CODEC: BuilderCodec<DatabaseConfig> = BuilderCodec.builder(
            DatabaseConfig::class.java,
            ::DatabaseConfig
        )
            .append(
                KeyedCodec("JdbcUrl", Codec.STRING),
                { config, value -> config.jdbcUrl = value },
                { config -> config.jdbcUrl }
            ).add()
            .append(
                KeyedCodec("DriverClassName", Codec.STRING),
                { config, value -> config.driverClassName = value },
                { config -> config.driverClassName }
            ).add()
            .append(
                KeyedCodec("Username", Codec.STRING),
                { config, value -> config.username = value },
                { config -> config.username }
            ).add()
            .append(
                KeyedCodec("Password", Codec.STRING),
                { config, value -> config.password = value },
                { config -> config.password }
            ).add()
            .append(
                KeyedCodec("MaximumPoolSize", Codec.INTEGER),
                { config, value -> config.maximumPoolSize = value },
                { config -> config.maximumPoolSize }
            ).add()
            .append(
                KeyedCodec("MinimumIdle", Codec.INTEGER),
                { config, value -> config.minimumIdle = value },
                { config -> config.minimumIdle }
            ).add()
            .append(
                KeyedCodec("IdleTimeout", Codec.LONG),
                { config, value -> config.idleTimeout = value },
                { config -> config.idleTimeout }
            ).add()
            .append(
                KeyedCodec("ConnectionTimeout", Codec.LONG),
                { config, value -> config.connectionTimeout = value },
                { config -> config.connectionTimeout }
            ).add()
            .append(
                KeyedCodec("MaxLifetime", Codec.LONG),
                { config, value -> config.maxLifetime = value },
                { config -> config.maxLifetime }
            ).add()
            .build()
    }
}

/**
 * Main configuration for the plot plugin.
 */
class PlotConfig {
    // World settings
    var worldName: String = "plotworld"
    var plotSize: Int = 32
    var roadWidth: Int = 5
    var groundHeight: Int = 64

    // Limits
    var defaultPlotLimit: Int = 1
    var maxPlotLimit: Int = 10

    // Block types (Hytale block type names)
    var plotBlockType: String = "Soil_Grass"
    var roadBlockType: String = "Rock_Stone_Cobble"
    var bedrockBlockType: String = "Rock_Volcanic"
    var borderBlockType: String = "Rock_Stone_Cobble_Half"
    var fillBlockType: String = "Rock_Stone"
    var dirtBlockType: String = "Soil_Dirt"

    // Layer depths
    var dirtDepth: Int = 4
    var bedrockDepth: Int = 3

    // Border configuration
    var borderEnabled: Boolean = true

    // World environment (must be a valid Hytale environment like "Surface", "Underground", etc.)
    var environment: String = "Surface"

    // Features
    var enableMerging: Boolean = true
    var enableReset: Boolean = true
    var claimFromRoad: Boolean = false
    var autoCreateWorld: Boolean = true

    // Permission-based plot limits
    // Players with permission "souza.plot.limit.<number>" will have that number as their limit
    // For example: "souza.plot.limit.3" allows claiming 3 plots
    var plotLimitPermissionPrefix: String = "souza.plot.limit."

    // Bottom layer protection (prevents void deaths)
    var protectBottomLayer: Boolean = true
    var protectedLayerHeight: Int = 1

    // Database
    var database: DatabaseConfig = DatabaseConfig()

    // Messages
    var messages: PlotMessages = PlotMessages()

    companion object {
        @JvmField
        val CODEC: BuilderCodec<PlotConfig> = BuilderCodec.builder(
            PlotConfig::class.java,
            ::PlotConfig
        )
            // World settings
            .append(
                KeyedCodec("WorldName", Codec.STRING),
                { config, value -> config.worldName = value },
                { config -> config.worldName }
            ).add()
            .append(
                KeyedCodec("PlotSize", Codec.INTEGER),
                { config, value -> config.plotSize = value },
                { config -> config.plotSize }
            ).add()
            .append(
                KeyedCodec("RoadWidth", Codec.INTEGER),
                { config, value -> config.roadWidth = value },
                { config -> config.roadWidth }
            ).add()
            .append(
                KeyedCodec("GroundHeight", Codec.INTEGER),
                { config, value -> config.groundHeight = value },
                { config -> config.groundHeight }
            ).add()
            // Limits
            .append(
                KeyedCodec("DefaultPlotLimit", Codec.INTEGER),
                { config, value -> config.defaultPlotLimit = value },
                { config -> config.defaultPlotLimit }
            ).add()
            .append(
                KeyedCodec("MaxPlotLimit", Codec.INTEGER),
                { config, value -> config.maxPlotLimit = value },
                { config -> config.maxPlotLimit }
            ).add()
            // Block types
            .append(
                KeyedCodec("PlotBlockType", Codec.STRING),
                { config, value -> config.plotBlockType = value },
                { config -> config.plotBlockType }
            ).add()
            .append(
                KeyedCodec("RoadBlockType", Codec.STRING),
                { config, value -> config.roadBlockType = value },
                { config -> config.roadBlockType }
            ).add()
            .append(
                KeyedCodec("BedrockBlockType", Codec.STRING),
                { config, value -> config.bedrockBlockType = value },
                { config -> config.bedrockBlockType }
            ).add()
            .append(
                KeyedCodec("BorderBlockType", Codec.STRING),
                { config, value -> config.borderBlockType = value },
                { config -> config.borderBlockType }
            ).add()
            .append(
                KeyedCodec("FillBlockType", Codec.STRING),
                { config, value -> config.fillBlockType = value },
                { config -> config.fillBlockType }
            ).add()
            .append(
                KeyedCodec("DirtBlockType", Codec.STRING),
                { config, value -> config.dirtBlockType = value },
                { config -> config.dirtBlockType }
            ).add()
            // Layer depths
            .append(
                KeyedCodec("DirtDepth", Codec.INTEGER),
                { config, value -> config.dirtDepth = value },
                { config -> config.dirtDepth }
            ).add()
            .append(
                KeyedCodec("BedrockDepth", Codec.INTEGER),
                { config, value -> config.bedrockDepth = value },
                { config -> config.bedrockDepth }
            ).add()
            // Border configuration
            .append(
                KeyedCodec("BorderEnabled", Codec.BOOLEAN),
                { config, value -> config.borderEnabled = value },
                { config -> config.borderEnabled }
            ).add()
            // Environment
            .append(
                KeyedCodec("Environment", Codec.STRING),
                { config, value -> config.environment = value },
                { config -> config.environment }
            ).add()
            // Features
            .append(
                KeyedCodec("EnableMerging", Codec.BOOLEAN),
                { config, value -> config.enableMerging = value },
                { config -> config.enableMerging }
            ).add()
            .append(
                KeyedCodec("EnableReset", Codec.BOOLEAN),
                { config, value -> config.enableReset = value },
                { config -> config.enableReset }
            ).add()
            .append(
                KeyedCodec("ClaimFromRoad", Codec.BOOLEAN),
                { config, value -> config.claimFromRoad = value },
                { config -> config.claimFromRoad }
            ).add()
            .append(
                KeyedCodec("AutoCreateWorld", Codec.BOOLEAN),
                { config, value -> config.autoCreateWorld = value },
                { config -> config.autoCreateWorld }
            ).add()
            // Permission-based limits
            .append(
                KeyedCodec("PlotLimitPermissionPrefix", Codec.STRING),
                { config, value -> config.plotLimitPermissionPrefix = value },
                { config -> config.plotLimitPermissionPrefix }
            ).add()
            // Bottom layer protection
            .append(
                KeyedCodec("ProtectBottomLayer", Codec.BOOLEAN),
                { config, value -> config.protectBottomLayer = value },
                { config -> config.protectBottomLayer }
            ).add()
            .append(
                KeyedCodec("ProtectedLayerHeight", Codec.INTEGER),
                { config, value -> config.protectedLayerHeight = value },
                { config -> config.protectedLayerHeight }
            ).add()
            // Database
            .append(
                KeyedCodec("Database", DatabaseConfig.CODEC),
                { config, value -> config.database = value },
                { config -> config.database }
            ).add()
            // Messages
            .append(
                KeyedCodec("Messages", PlotMessages.CODEC),
                { config, value -> config.messages = value },
                { config -> config.messages }
            ).add()
            .build()
    }
}
