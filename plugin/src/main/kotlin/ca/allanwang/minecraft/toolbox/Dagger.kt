package ca.allanwang.minecraft.toolbox

import ca.allanwang.minecraft.toolbox.base.Mct
import ca.allanwang.minecraft.toolbox.base.MctNode
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
    @RootNode
    fun rootNodes(): Map<String, MctNode>

    @PluginScope
    fun mct(): Mct

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun plugin(plugin: Plugin): Builder

        @BindsInstance
        fun mct(mct: Mct): Builder

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
    fun mctNode(mct: MctRootNode): MctNode = mct

    @Provides
    @RootNode
    fun rootNodes(@RootNode rootNodes: Set<@JvmSuppressWildcards MctNode>): Map<String, MctNode> =
        rootNodes.associateBy {
            it.name.toLowerCase(
                Locale.ENGLISH
            )
        }
}
