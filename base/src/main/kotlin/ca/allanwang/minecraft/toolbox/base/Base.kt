package ca.allanwang.minecraft.toolbox.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

interface Mct {
    val mctScope: CoroutineScope
    val mctLogger: Logger
    val events: SharedFlow<Event>
}

inline fun <reified T : Event> Mct.on(
    scope: CoroutineScope = mctScope,
    noinline consumer: suspend (T) -> Unit
): Job =
    events.buffer(Channel.UNLIMITED).filterIsInstance<T>()
        .onEach {
            scope.launch {
                runCatching { consumer(it) }
                    .onFailure {
                        mctLogger.log(Level.WARNING, "Scope error", it)
                    }
            }
        }
        .launchIn(scope)

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
 * Check if location is below [loc].
 * Decimals are ignored, and note that y represents elevation axis.
 */
fun Location.isBelow(loc: Location): Boolean {
    return blockX == loc.blockX && blockY < loc.blockY && blockZ == loc.blockZ
}

val Block.isHighestBlock: Boolean
    get() = world.getHighestBlockYAt(location) <= y

fun Block.isNear(material: Material, range: Int): Boolean {
    (-range..range).forEach { x ->
        (-range..range).forEach { y ->
            (-range..range).forEach { z ->
                if (getRelative(x, y, z).type == material) return true
            }
        }
    }
    return false
}

val Player.blockBelow: Block
    get() = world.getBlockAt(
        location.blockX,
        location.blockY - 1,
        location.blockZ
    )

fun Block.faceSequence(face: BlockFace): Sequence<Block> =
    generateSequence(this) {
        it.getRelative(face)
    }

fun String.toLowerCaseMct() = toLowerCase(Locale.US)