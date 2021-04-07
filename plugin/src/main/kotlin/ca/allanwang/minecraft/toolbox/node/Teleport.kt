package ca.allanwang.minecraft.toolbox.node

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.base.TabCompleteContext
import org.bukkit.block.BlockFace
import javax.inject.Inject

@PluginScope
class Teleport @Inject internal constructor(
    facing: Facing, up: Up, down: Down
) : MctNode(name = "teleport", aliases = setOf("tp")) {

    init {
        children(facing, up, down)
    }

    override suspend fun CommandContext.command() {
        val player = args.firstOrNull().nameToPlayerOrNull() ?: return
        if (player.isInsideVehicle)
            return echo("Cannot teleport to ${player.name} as they are inside a vehicle")
        sender.teleport(player)
        echo("Teleported to ${player.name}")
    }

    override fun TabCompleteContext.tabComplete(): List<String>? =
        defaultTabComplete()?.takeIf { it.isNotEmpty() }
            ?.let { listOf("<player>") + it }

    @PluginScope
    class Facing @Inject internal constructor(
    ) : MctNode(name = "facing") {
        override suspend fun CommandContext.command() {
            logger.info { "Face ${sender.facing} ${sender.location.yaw}" }
            teleportRelative(sender.facing)
        }
    }

    @PluginScope
    class Up @Inject internal constructor(
    ) : MctNode(name = "up") {
        override suspend fun CommandContext.command() {
            teleportRelative(BlockFace.UP)
        }
    }

    @PluginScope
    class Down @Inject internal constructor(
    ) : MctNode(name = "down") {
        override suspend fun CommandContext.command() {
            teleportRelative(BlockFace.DOWN)
        }
    }
}

private const val MAX_RELATIVE_TRAVEL_DISTANCE = 5

private suspend fun CommandContext.travelDistance(): Int {
    val distance = args.firstOrNull()?.toIntOrNull()
        ?: fail("Please supply travel distance (max $MAX_RELATIVE_TRAVEL_DISTANCE)")
    if (distance > MAX_RELATIVE_TRAVEL_DISTANCE || distance < -MAX_RELATIVE_TRAVEL_DISTANCE) fail(
        "Max travel distance is 5, selected $distance"
    )
    return distance
}

/**
 * Move in the provided direction, while keeping pitch (vertical head tilt) and yaw (horizontal head tilt)
 */
private suspend fun CommandContext.teleportRelative(direction: BlockFace) {
    val distance = travelDistance()
    val toBlock =
        sender.location.block.getRelative(direction, distance)
    if (!toBlock.isPassable) {
        echo("Destination not passable (${toBlock.type.name})")
        return
    }
    val loc = toBlock.location.clone()
    loc.pitch = sender.location.pitch
    loc.yaw = sender.location.yaw
    sender.teleport(loc)
}