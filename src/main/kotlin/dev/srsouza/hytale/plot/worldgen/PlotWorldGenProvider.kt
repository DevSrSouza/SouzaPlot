package dev.srsouza.hytale.plot.worldgen

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.math.vector.Transform
import com.hypixel.hytale.protocol.Color
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil
import com.hypixel.hytale.server.core.codec.ProtocolCodecs
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider
import javax.annotation.Nonnull

/**
 * Custom world generation provider for plot worlds.
 * Generates a flat world with plots and roads.
 */
class PlotWorldGenProvider : IWorldGenProvider {

    companion object {
        const val ID = "Plot"

        @JvmField
        val CODEC: BuilderCodec<PlotWorldGenProvider> = BuilderCodec.builder(
            PlotWorldGenProvider::class.java,
            ::PlotWorldGenProvider
        )
            .documentation("A world generation provider that generates a flat world with plots and roads.")
            .append(
                KeyedCodec("Tint", ProtocolCodecs.COLOR),
                { config, value -> config.tint = value },
                { config -> config.tint }
            )
            .documentation("The tint color for all chunks.")
            .add()
            .append(
                KeyedCodec("PlotSize", Codec.INTEGER),
                { config, value -> config.plotSize = value },
                { config -> config.plotSize }
            )
            .documentation("The size of each plot in blocks (32 = 32x32).")
            .add()
            .append(
                KeyedCodec("RoadWidth", Codec.INTEGER),
                { config, value -> config.roadWidth = value },
                { config -> config.roadWidth }
            )
            .documentation("The width of roads between plots in blocks.")
            .add()
            .append(
                KeyedCodec("GroundHeight", Codec.INTEGER),
                { config, value -> config.groundHeight = value },
                { config -> config.groundHeight }
            )
            .documentation("The Y level of the ground surface.")
            .add()
            .append(
                KeyedCodec("PlotBlockType", Codec.STRING),
                { config, value -> config.plotBlockType = value },
                { config -> config.plotBlockType }
            )
            .documentation("The block type for plot surfaces.")
            .add()
            .append(
                KeyedCodec("RoadBlockType", Codec.STRING),
                { config, value -> config.roadBlockType = value },
                { config -> config.roadBlockType }
            )
            .documentation("The block type for roads.")
            .add()
            .append(
                KeyedCodec("BedrockBlockType", Codec.STRING),
                { config, value -> config.bedrockBlockType = value },
                { config -> config.bedrockBlockType }
            )
            .documentation("The block type for the bedrock layer.")
            .add()
            .append(
                KeyedCodec("BorderBlockType", Codec.STRING),
                { config, value -> config.borderBlockType = value },
                { config -> config.borderBlockType }
            )
            .documentation("The block type for plot borders.")
            .add()
            .append(
                KeyedCodec("FillBlockType", Codec.STRING),
                { config, value -> config.fillBlockType = value },
                { config -> config.fillBlockType }
            )
            .documentation("The block type for filling under the surface (stone layer).")
            .add()
            .append(
                KeyedCodec("DirtBlockType", Codec.STRING),
                { config, value -> config.dirtBlockType = value },
                { config -> config.dirtBlockType }
            )
            .documentation("The block type for the dirt layer under plots.")
            .add()
            .append(
                KeyedCodec("DirtDepth", Codec.INTEGER),
                { config, value -> config.dirtDepth = value },
                { config -> config.dirtDepth }
            )
            .documentation("The depth of the dirt layer under plot surfaces (default: 4).")
            .add()
            .append(
                KeyedCodec("BedrockDepth", Codec.INTEGER),
                { config, value -> config.bedrockDepth = value },
                { config -> config.bedrockDepth }
            )
            .documentation("The depth of the bedrock layer at the bottom (default: 3).")
            .add()
            .append(
                KeyedCodec("BorderEnabled", Codec.BOOLEAN),
                { config, value -> config.borderEnabled = value },
                { config -> config.borderEnabled }
            )
            .documentation("Whether to generate border walls around plots.")
            .add()
            .append(
                KeyedCodec("Environment", Codec.STRING),
                { config, value -> config.environment = value },
                { config -> config.environment }
            )
            .documentation("The environment type for the world.")
            .add()
            .build()

        val DEFAULT_TINT = Color(91.toByte(), (-98).toByte(), 40.toByte())
    }

    // Configuration properties
    var tint: Color = DEFAULT_TINT
    var plotSize: Int = 32
    var roadWidth: Int = 5
    var groundHeight: Int = 64
    var plotBlockType: String = "Soil_Grass"
    var roadBlockType: String = "Rock_Stone_Cobble"
    var bedrockBlockType: String = "Rock_Volcanic"
    var borderBlockType: String = "Rock_Stone_Cobble_Half"
    var fillBlockType: String = "Rock_Stone"
    var dirtBlockType: String = "Soil_Dirt"
    var dirtDepth: Int = 4
    var bedrockDepth: Int = 3
    var borderEnabled: Boolean = true
    var environment: String = "Surface"

    @Nonnull
    @Throws(WorldGenLoadException::class)
    override fun getGenerator(): IWorldGen {
        // Resolve block IDs
        val plotBlockId = resolveBlockId(plotBlockType, "PlotBlockType")
        val roadBlockId = resolveBlockId(roadBlockType, "RoadBlockType")
        val bedrockBlockId = resolveBlockId(bedrockBlockType, "BedrockBlockType")
        val borderBlockId = resolveBlockId(borderBlockType, "BorderBlockType")
        val fillBlockId = resolveBlockId(fillBlockType, "FillBlockType")
        val dirtBlockId = resolveBlockId(dirtBlockType, "DirtBlockType")

        // Resolve environment ID with fallback
        var environmentId = Environment.getAssetMap().getIndex(environment)
        if (environmentId == Int.MIN_VALUE) {
            // Try fallback environments
            val fallbackEnvironments = listOf("Surface", "Default", "Underground")
            for (fallback in fallbackEnvironments) {
                environmentId = Environment.getAssetMap().getIndex(fallback)
                if (environmentId != Int.MIN_VALUE) {
                    break
                }
            }
            // If still not found, use index 0 (first available environment)
            if (environmentId == Int.MIN_VALUE) {
                environmentId = 0
            }
        }

        // Convert tint color to ARGB int
        val tintId = ColorParseUtil.colorToARGBInt(tint)

        return PlotWorldGen(
            plotSize = plotSize,
            roadWidth = roadWidth,
            groundHeight = groundHeight,
            plotBlockId = plotBlockId,
            roadBlockId = roadBlockId,
            bedrockBlockId = bedrockBlockId,
            borderBlockId = borderBlockId,
            fillBlockId = fillBlockId,
            dirtBlockId = dirtBlockId,
            bedrockDepth = bedrockDepth,
            dirtDepth = dirtDepth,
            borderEnabled = borderEnabled,
            environmentId = environmentId,
            tintId = tintId
        )
    }

    @Throws(WorldGenLoadException::class)
    private fun resolveBlockId(blockTypeName: String, configName: String): Int {
        val blockId = BlockType.getAssetMap().getIndex(blockTypeName)
        if (blockId == Int.MIN_VALUE) {
            throw WorldGenLoadException("Unknown block type for $configName: $blockTypeName")
        }
        return blockId
    }

    @Nonnull
    override fun toString(): String {
        return "PlotWorldGenProvider{plotSize=$plotSize, roadWidth=$roadWidth, groundHeight=$groundHeight}"
    }
}
