package ca.allanwang.minecraft.toolbox.base

import org.bukkit.Location
import org.bukkit.event.block.Action
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable
import org.bukkit.plugin.Plugin

inline fun <reified T> Metadatable.metadata(key: String, plugin: Plugin): T? =
    getMetadata(key).firstOrNull { it.owningPlugin == plugin }
        ?.value() as? T?

fun <T> Metadatable.metadata(key: String, plugin: Plugin, value: T?) {
    setMetadata(key, FixedMetadataValue(plugin, value))
}

val Action.isLeftClick: Boolean
    get() = this == Action.LEFT_CLICK_AIR || this == Action.LEFT_CLICK_BLOCK

val Action.isRightClick: Boolean
    get() = this == Action.RIGHT_CLICK_AIR || this == Action.RIGHT_CLICK_BLOCK

fun Location.toPrettyString() = buildString {
    append("[")
    world?.name?.let {
        append(it)
        append(" ")
    }
    append(blockX)
    append(", ")
    append(blockY)
    append(", ")
    append(blockZ)
    append("]")
}

/**
 * Check if location is just below [loc].
 * Decimals are ignored, and note that y represents elevation axis.
 */
fun Location.isBelow(loc: Location): Boolean {
    return blockX == loc.blockX && blockY == loc.blockY - 1 && blockZ == loc.blockZ
}