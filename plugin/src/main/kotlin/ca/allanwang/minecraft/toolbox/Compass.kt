package ca.allanwang.minecraft.toolbox

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.Mct
import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.base.faceSequence
import ca.allanwang.minecraft.toolbox.base.isBelow
import ca.allanwang.minecraft.toolbox.base.isHighestBlock
import ca.allanwang.minecraft.toolbox.base.isRightClick
import ca.allanwang.minecraft.toolbox.base.metadata
import ca.allanwang.minecraft.toolbox.base.on
import ca.allanwang.minecraft.toolbox.base.toPrettyString
import dagger.Module
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Logger
import javax.inject.Inject

@PluginScope
class CompassHelper @Inject internal constructor(
    private val plugin: Plugin,
    private val server: Server,
    private val logger: Logger,
    private val mct: Mct,
) {

    companion object {
        /**
         * Key for player list that's tracking current player
         */
        private const val KEY_TRACKERS = "mct_compass_player_trackers"

        /**
         * Key for player to track
         */
        private const val KEY_TRACKING = "mct_compass_player_tracking"

        private const val BEACON_HEIGHT = 30

        private const val BEACON_METADATA_KEY = "beacon_block_item"
    }

    private var Player.compassTrackers: Set<UUID>
        get() = metadata(KEY_TRACKERS, plugin) ?: emptySet()
        set(value) {
            metadata(KEY_TRACKERS, plugin, value)
        }

    private var Player.compassTracking: UUID?
        get() = metadata(KEY_TRACKING, plugin)
        set(value) {
            metadata(KEY_TRACKING, plugin, value)
        }

    init {
        mct.on<PlayerInteractEvent> { handleBeacon(it) }
        mct.on<PlayerMoveEvent> { updateCompass(it) }
        mct.on<BlockBreakEvent> { checkBeaconInteractionEvent(it) }
    }

    private fun removeTracking(player: Player) {
        val trackingPlayer = player.compassTracking?.let {
            server.getPlayer(it)
        } ?: return
        player.compassTracking = null
        removeTracker(trackingPlayer, player)
    }

    fun setTracking(player: Player, playerToTrack: Player?) {
        removeTracking(player)
        if (playerToTrack == null) return
        player.compassTracking = playerToTrack.uniqueId
        playerToTrack.compassTrackers =
            playerToTrack.compassTrackers + playerToTrack.uniqueId
    }

    fun removeTracker(player: Player, tracker: Player) {
        player.compassTrackers =
            player.compassTrackers.filter { it != tracker.uniqueId }.toSet()
    }

    private fun updateCompass(event: PlayerMoveEvent) {
        event.player.compassTrackers.mapNotNull { server.getPlayer(it) }
            .forEach {
                it.compassTarget = event.player.location
            }
    }

    private var Block.isBeaconBlock: Boolean?
        get() = metadata<Boolean>(BEACON_METADATA_KEY, plugin)
        set(value) {
            if (value == null) removeMetadata(BEACON_METADATA_KEY, plugin)
            else metadata(BEACON_METADATA_KEY, plugin, value)
        }

    private fun handleBeacon(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return
        // Cancel offhand usage if compass is in main hand
        if (event.hand == EquipmentSlot.OFF_HAND && event.player.inventory.itemInMainHand.type == Material.COMPASS) {
            event.isCancelled = true
            return
        }
        if (event.material != Material.COMPASS) return
        val player = event.player
        val clickedBlocked = event.clickedBlock ?: return
        if (event.player.isSneaking) {
            if (isBeacon(clickedBlocked)) return deleteBeacon(player, clickedBlocked)
            if (showBeacon(player, clickedBlocked)) return
        }
        if (event.clickedBlock?.location?.isBelow(event.player.location) == true) {
            // Broadcast location
            server.onlinePlayers.forEach {
                it.sendMessage("${event.player.name} is at ${event.player.location.toPrettyString()}")
            }
            return
        }
        event.player.sendMessage(event.player.location.toPrettyString())
    }

    /**
     * Show beacon directly above clicked block
     */
    private fun showBeacon(player: Player, block: Block): Boolean {
        if (!block.isHighestBlock) return false
        if (block.getRelative(
                BlockFace.UP,
                BEACON_HEIGHT
            ).isBeaconBlock == true
        ) return false
        // Create beacon
        logger.info { "Create beacon ${block.location.toPrettyString()}" }
        player.sendMessage("Created beacon at ${block.location.toPrettyString()}")
        (5..5 + BEACON_HEIGHT).forEach {
            val b = block.getRelative(BlockFace.UP, it)
            b.setType(Material.TORCH, false)
            b.isBeaconBlock = true
        }
        return true
    }

    private fun isBeacon(block: Block): Boolean {
        if (block.isBeaconBlock == true) return true
        if (block.isBeaconBlock == false) return false // pending deletion
        if (block.type != Material.TORCH) return false
        val blockBelowChain = block.faceSequence(BlockFace.DOWN)
            .first { it.type != Material.TORCH }
        if ((1..BEACON_HEIGHT).all {
                blockBelowChain.getRelative(
                    BlockFace.UP,
                    it
                ).type == Material.TORCH
            }) {
            blockBelowChain.getRelative(BlockFace.UP).faceSequence(BlockFace.UP)
                .takeWhile { it.type == Material.TORCH }
                .forEach { it.isBeaconBlock = true }
            return true
        }
        return false
    }

    private fun deleteBeacon(player: Player, block: Block) {
        logger.info { "Delete beacon ${block.location.toPrettyString()}" }
        player.sendMessage("Deleted beacon at ${block.location.toPrettyString()}")
        if (block.isBeaconBlock != true) return
        block.faceSequence(BlockFace.UP)
            .takeWhile { it.isBeaconBlock == true }
            .last()
            .faceSequence(BlockFace.DOWN)
            .takeWhile { it.isBeaconBlock == true }
            .forEach {
                it.isBeaconBlock = null
                it.setType(Material.AIR, false)
            }
    }

    private fun checkBeaconInteractionEvent(event: BlockBreakEvent) {
        if (event.block.isBeaconBlock == true) {
            event.isCancelled = true
        }
    }

    private fun checkBeaconDestructionEvent(event: ItemSpawnEvent) {
        if (event.entityType != EntityType.DROPPED_ITEM) return
        if (event.entity.itemStack.type != Material.TORCH) return
        logger.info { "Dropped torch" }
        event.entity.remove()
    }
}


@PluginScope
class Compass @Inject internal constructor(
    follow: Follow,
    reset: Reset,
) : MctNode(name = "compass") {

    init {
        children(follow, reset)
    }

    @PluginScope
    class Follow @Inject internal constructor(
        private val compassHelper: CompassHelper
    ) : MctNode(name = "follow") {

        override suspend fun CommandContext.command() {
            val playerToFollow = args.firstOrNull().nameToPlayer()
            logger.info { "${sender.name}'s compass will follow ${playerToFollow.name}" }
            sender.compassTarget = playerToFollow.location
            compassHelper.setTracking(sender, playerToFollow)
            echo("Set compass to follow ${playerToFollow.name}")
        }

    }

    @PluginScope
    class Reset @Inject internal constructor(
        private val compassHelper: CompassHelper
    ) : MctNode(name = "reset") {

        override suspend fun CommandContext.command() {
            logger.info { "${sender.name}'s compass reset to ${sender.world.spawnLocation}" }
            sender.compassTarget = sender.world.spawnLocation
            compassHelper.setTracking(sender, null)
            echo("Compass has been reset")
        }

    }

}

@Module
object CompassModule