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
        defaultTabComplete()?.let { listOf("<player>") + it }

    @PluginScope
    class Facing @Inject internal constructor(
    ) : MctNode(name = "facing") {
        override suspend fun CommandContext.command() {
            teleportRelative(sender.facing)
        }
    }

    @PluginScope
    class Up @Inject internal constructor(
    ) : MctNode(name = "facing") {
        override suspend fun CommandContext.command() {
            teleportRelative(BlockFace.UP)
        }
    }

    @PluginScope
    class Down @Inject internal constructor(
    ) : MctNode(name = "facing") {
        override suspend fun CommandContext.command() {
            teleportRelative(BlockFace.DOWN)
        }
    }
}

private suspend fun CommandContext.travelDistance(): Int {
    val distance = args.firstOrNull()?.toIntOrNull()
        ?: fail("Please supply travel distance (max 5)")
    if (distance > 5 || distance < -5) fail("Max travel distance is 5, selected $distance")
    return distance
}

private suspend fun CommandContext.teleportRelative(direction: BlockFace) {
    val distance = travelDistance()
    val toBlock =
        sender.location.block.getRelative(direction, distance)
    if (!toBlock.isEmpty && !toBlock.isLiquid) {
        echo("Destination is not air or liquid")
        return
    }
    sender.teleport(toBlock.location)
}