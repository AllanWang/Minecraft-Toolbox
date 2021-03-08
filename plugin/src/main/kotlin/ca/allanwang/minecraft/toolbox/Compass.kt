package ca.allanwang.minecraft.toolbox

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.MctPlayerMoveHandler
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.base.metadata
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Logger
import javax.inject.Inject

@PluginScope
class CompassHelper @Inject internal constructor(
    private val plugin: Plugin,
    private val server: Server,
    private val logger: Logger
) : MctPlayerMoveHandler {

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

    override fun onPlayerMove(event: PlayerMoveEvent) {
        event.player.compassTrackers.mapNotNull { server.getPlayer(it) }
            .forEach {
                it.compassTarget = event.player.location
            }
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
}