package dev.srsouza.hytale.plot.model

import com.hypixel.hytale.math.vector.Vector3d
import java.util.UUID

/**
 * Represents a claimed plot with its owner, members, and settings.
 */
data class Plot(
    /** The plot's grid coordinates */
    val id: PlotId,
    /** The owner's UUID (null = admin/server plot) */
    var owner: UUID?,
    /** Set of members with their trust levels */
    val members: MutableSet<PlotMember> = mutableSetOf(),
    /** Set of denied/banned player UUIDs */
    val denied: MutableSet<UUID> = mutableSetOf(),
    /** Plot-specific settings */
    var settings: PlotSettings = PlotSettings.default(),
    /** Custom home/spawn location within the plot */
    var homeLocation: Vector3d? = null,
    /** Optional plot name/alias */
    var alias: String? = null,
    /** Set of merged plot IDs (for merged plots) */
    val mergedWith: MutableSet<PlotId> = mutableSetOf(),
    /** Timestamp when the plot was claimed */
    val createdAt: Long = System.currentTimeMillis(),
    /** Timestamp of last activity on the plot */
    var lastActivity: Long = System.currentTimeMillis()
) {
    /**
     * Checks if the given UUID is the owner of this plot.
     */
    fun isOwner(uuid: UUID): Boolean = owner == uuid

    /**
     * Checks if the given UUID is a member of this plot.
     */
    fun isMember(uuid: UUID): Boolean = members.any { it.uuid == uuid }

    /**
     * Gets the member data for a UUID, if they are a member.
     */
    fun getMember(uuid: UUID): PlotMember? = members.find { it.uuid == uuid }

    /**
     * Checks if the given UUID can build on this plot.
     * Returns true if they are the owner or a member with build permission.
     */
    fun canBuild(uuid: UUID): Boolean {
        if (isOwner(uuid)) return true
        return getMember(uuid)?.canBuild() == true
    }

    /**
     * Checks if the given UUID can use containers on this plot.
     */
    fun canUseContainers(uuid: UUID): Boolean {
        if (isOwner(uuid)) return true
        return getMember(uuid)?.canUseContainers() == true
    }

    /**
     * Checks if the given UUID is denied from this plot.
     */
    fun isDenied(uuid: UUID): Boolean = denied.contains(uuid)

    /**
     * Checks if this is an admin/server plot (no player owner).
     */
    fun isAdminPlot(): Boolean = owner == null

    /**
     * Gets all effective plots (this plot plus any merged plots).
     */
    fun getEffectivePlots(): Set<PlotId> {
        return setOf(id) + mergedWith
    }

    /**
     * Updates the last activity timestamp.
     */
    fun touch() {
        lastActivity = System.currentTimeMillis()
    }

    /**
     * Adds a member to the plot.
     * @return true if the member was added, false if already a member
     */
    fun addMember(uuid: UUID, trustLevel: TrustLevel): Boolean {
        if (isMember(uuid) || isOwner(uuid)) return false
        members.add(PlotMember(uuid, trustLevel))
        touch()
        return true
    }

    /**
     * Removes a member from the plot.
     * @return true if the member was removed, false if not found
     */
    fun removeMember(uuid: UUID): Boolean {
        val removed = members.removeIf { it.uuid == uuid }
        if (removed) touch()
        return removed
    }

    /**
     * Denies a player from the plot.
     * @return true if the player was denied, false if already denied
     */
    fun denyPlayer(uuid: UUID): Boolean {
        if (isDenied(uuid)) return false
        denied.add(uuid)
        // Also remove them as a member if they are one
        members.removeIf { it.uuid == uuid }
        touch()
        return true
    }

    /**
     * Un-denies a player from the plot.
     * @return true if the player was un-denied, false if not found
     */
    fun undenyPlayer(uuid: UUID): Boolean {
        val removed = denied.remove(uuid)
        if (removed) touch()
        return removed
    }
}
