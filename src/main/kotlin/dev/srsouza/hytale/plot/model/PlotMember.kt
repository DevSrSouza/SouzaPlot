package dev.srsouza.hytale.plot.model

import java.util.UUID

/**
 * Trust level for plot members.
 */
enum class TrustLevel {
    /** Full build access */
    TRUSTED,
    /** Can build but cannot use containers */
    HELPER,
    /** Can visit but cannot build */
    VISITOR
}

/**
 * Represents a member of a plot with their trust level.
 */
data class PlotMember(
    val uuid: UUID,
    val trustLevel: TrustLevel,
    val addedAt: Long = System.currentTimeMillis()
) {
    /**
     * Checks if this member can build on the plot.
     */
    fun canBuild(): Boolean = trustLevel == TrustLevel.TRUSTED || trustLevel == TrustLevel.HELPER

    /**
     * Checks if this member can use containers on the plot.
     */
    fun canUseContainers(): Boolean = trustLevel == TrustLevel.TRUSTED
}
