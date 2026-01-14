package dev.srsouza.hytale.plot.database.tables

import org.jetbrains.exposed.sql.Table

/**
 * Database table for plots.
 */
object PlotsTable : Table("plots") {
    val plotX = integer("plot_x")
    val plotZ = integer("plot_z")
    val owner = uuid("owner").nullable()
    val alias = varchar("alias", 64).nullable()
    val homeX = double("home_x").nullable()
    val homeY = double("home_y").nullable()
    val homeZ = double("home_z").nullable()
    val pvp = bool("pvp").default(false)
    val mobSpawning = bool("mob_spawning").default(false)
    val weather = bool("weather").default(true)
    val fixedTime = integer("fixed_time").nullable()
    val entryMessage = varchar("entry_message", 256).nullable()
    val exitMessage = varchar("exit_message", 256).nullable()
    val gameMode = varchar("game_mode", 32).nullable()
    val createdAt = long("created_at")
    val lastActivity = long("last_activity")

    override val primaryKey = PrimaryKey(plotX, plotZ)
}

/**
 * Database table for plot members.
 */
object PlotMembersTable : Table("plot_members") {
    val plotX = integer("plot_x")
    val plotZ = integer("plot_z")
    val memberUuid = uuid("member_uuid")
    val trustLevel = varchar("trust_level", 16)
    val addedAt = long("added_at")

    override val primaryKey = PrimaryKey(plotX, plotZ, memberUuid)
}

/**
 * Database table for denied players.
 */
object PlotDeniedTable : Table("plot_denied") {
    val plotX = integer("plot_x")
    val plotZ = integer("plot_z")
    val deniedUuid = uuid("denied_uuid")

    override val primaryKey = PrimaryKey(plotX, plotZ, deniedUuid)
}

/**
 * Database table for merged plots.
 */
object PlotMergedTable : Table("plot_merged") {
    val plotX = integer("plot_x")
    val plotZ = integer("plot_z")
    val mergedX = integer("merged_x")
    val mergedZ = integer("merged_z")

    override val primaryKey = PrimaryKey(plotX, plotZ, mergedX, mergedZ)
}

/**
 * Database table for storing player's last location before teleporting to a plot.
 * Used by /plot tpback to return players to their previous location.
 */
object PlayerLastLocationsTable : Table("player_last_locations") {
    val playerUuid = uuid("player_uuid")
    val worldName = varchar("world_name", 64)
    val x = double("x")
    val y = double("y")
    val z = double("z")
    val yaw = float("yaw")
    val pitch = float("pitch")
    val savedAt = long("saved_at")

    override val primaryKey = PrimaryKey(playerUuid)
}
