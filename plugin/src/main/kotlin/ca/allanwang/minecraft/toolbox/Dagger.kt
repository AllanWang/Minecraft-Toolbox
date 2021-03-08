package ca.allanwang.minecraft.toolbox

import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.MctPlayerInteractionHandler
import ca.allanwang.minecraft.toolbox.base.MctPlayerMoveHandler
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.base.RootNode
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Logger

@Component(modules = [MctPluginModule::class])
@PluginScope
interface MctPluginComponent {

    @PluginScope
    fun logger(): Logger

    @PluginScope
    fun playerMoveHandlers(): Set<MctPlayerMoveHandler>

    @PluginScope
    fun playerInteractionHandlers(): Set<MctPlayerInteractionHandler>

    @PluginScope
    @RootNode
    fun rootNodes(): Map<String, MctNode>

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun plugin(plugin: Plugin): Builder

        fun build(): MctPluginComponent
    }
}

@Module(
    includes = [CompassModule::class],
)
object MctPluginModule {
    @Provides
    fun server(plugin: Plugin): Server = plugin.server

    @Provides
    fun logger(plugin: Plugin): Logger = plugin.logger

    @Provides
    @IntoSet
    @RootNode
    @PluginScope
    fun mct(mct: Mct): MctNode = mct

    @Provides
    @RootNode
    fun rootNodes(@RootNode rootNodes: Set<@JvmSuppressWildcards MctNode>): Map<String, MctNode> =
        rootNodes.associateBy {
            it.name.toLowerCase(
                Locale.ENGLISH
            )
        }
}
