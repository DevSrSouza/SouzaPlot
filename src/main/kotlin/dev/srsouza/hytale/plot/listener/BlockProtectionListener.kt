package dev.srsouza.hytale.plot.listener

import dev.srsouza.hytale.plot.PlotPlugin
import com.hypixel.hytale.component.Archetype
import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.EntityEventSystem
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.Message

/**
 * EntityEventSystem for handling block break events with plot protection.
 */
class BlockBreakProtectionSystem(
    private val plugin: PlotPlugin
) : EntityEventSystem<EntityStore, BreakBlockEvent>(BreakBlockEvent::class.java) {

    override fun handle(
        index: Int,
        archetypeChunk: ArchetypeChunk<EntityStore>,
        store: Store<EntityStore>,
        commandBuffer: CommandBuffer<EntityStore>,
        event: BreakBlockEvent
    ) {
        val playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType())
            ?: return

        val worldUuid = playerRef.worldUuid ?: return
        val world = com.hypixel.hytale.server.core.universe.Universe.get().getWorld(worldUuid) ?: return

        if (!plugin.worldManager.isPlotWorld(world)) return

        val targetBlock = event.targetBlock
        val x = targetBlock.x
        val y = targetBlock.y
        val z = targetBlock.z

        if (!plugin.protectionManager.canModify(playerRef.uuid, x, y, z)) {
            event.setCancelled(true)

            // Show specific message for bottom layer protection
            val message = if (plugin.protectionManager.isProtectedBottomLayer(y)) {
                plugin.config.messages.cannotBreakBottomLayer
            } else {
                plugin.config.messages.cannotBreakHere
            }
            playerRef.sendMessage(Message.raw(plugin.config.messages.format(message)))
        }
    }

    override fun getQuery(): Query<EntityStore>? = Archetype.of(PlayerRef.getComponentType())
}

/**
 * EntityEventSystem for handling block place events with plot protection.
 */
class BlockPlaceProtectionSystem(
    private val plugin: PlotPlugin
) : EntityEventSystem<EntityStore, PlaceBlockEvent>(PlaceBlockEvent::class.java) {

    override fun handle(
        index: Int,
        archetypeChunk: ArchetypeChunk<EntityStore>,
        store: Store<EntityStore>,
        commandBuffer: CommandBuffer<EntityStore>,
        event: PlaceBlockEvent
    ) {
        val playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType())
            ?: return

        val worldUuid = playerRef.worldUuid ?: return
        val world = com.hypixel.hytale.server.core.universe.Universe.get().getWorld(worldUuid) ?: return

        if (!plugin.worldManager.isPlotWorld(world)) return

        val targetBlock = event.targetBlock
        val x = targetBlock.x
        val y = targetBlock.y
        val z = targetBlock.z

        if (!plugin.protectionManager.canModify(playerRef.uuid, x, y, z)) {
            event.setCancelled(true)
            playerRef.sendMessage(Message.raw(plugin.config.messages.format(
                plugin.config.messages.cannotBuildHere
            )))
        }
    }

    override fun getQuery(): Query<EntityStore>? = Archetype.of(PlayerRef.getComponentType())
}
