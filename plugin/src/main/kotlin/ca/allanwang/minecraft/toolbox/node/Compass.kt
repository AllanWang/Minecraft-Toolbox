package ca.allanwang.minecraft.toolbox.node

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.helper.CompassHelper
import dagger.Module
import javax.inject.Inject


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
