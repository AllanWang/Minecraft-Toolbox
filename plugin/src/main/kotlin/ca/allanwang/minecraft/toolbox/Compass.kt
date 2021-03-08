package ca.allanwang.minecraft.toolbox

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.MctPlayerInteractionHandler
import ca.allanwang.minecraft.toolbox.base.MctPlayerMoveHandler
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.base.isBelow
import ca.allanwang.minecraft.toolbox.base.isRightClick
import ca.allanwang.minecraft.toolbox.base.metadata
import ca.allanwang.minecraft.toolbox.base.toPrettyString
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.Event
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
    private val logger: Logger
) : MctPlayerMoveHandler, MctPlayerInteractionHandler {

    companion object {
        /**
         * Key for player list that's tracking current player
         */
        private const val KEY_TRACKERS = "mct_compass_player_trackers"

        /**
         * Key for player to track
         */
        private const val KEY_TRACKING = "mct_compass_player_tracking"
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

    override fun onPlayerMove(event: PlayerMoveEvent) {
        event.player.compassTrackers.mapNotNull { server.getPlayer(it) }
            .forEach {
                it.compassTarget = event.player.location
            }
    }

    override fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return
        // Cancel offhand usage if compass is in main hand
        if (event.hand == EquipmentSlot.OFF_HAND && event.player.inventory.itemInMainHand.type == Material.COMPASS) {
            event.isCancelled = true
            return
        }
        if (event.material != Material.COMPASS) return
        event.item
        if (event.clickedBlock?.location?.isBelow(event.player.location) == true) {
            // Broadcast location
            server.onlinePlayers.forEach {
                it.sendMessage("${event.player.name} is at ${event.player.location.toPrettyString()}")
            }
            return
        }
        event.player.sendMessage(event.player.location.toPrettyString())
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
object CompassModule {
    @Provides
    @IntoSet
    @PluginScope
    fun compassHelperMove(compassHelper: CompassHelper): MctPlayerMoveHandler =
        compassHelper

    @Provides
    @IntoSet
    @PluginScope
    fun compassHelperInteract(compassHelper: CompassHelper): MctPlayerInteractionHandler =
        compassHelper
}