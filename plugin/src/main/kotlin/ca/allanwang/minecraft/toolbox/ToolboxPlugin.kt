@file:JvmName("ToolbarPlugin")

package ca.allanwang.minecraft.toolbox

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.TabCompleteContext
import com.github.shynixn.mccoroutine.setSuspendingExecutor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class ToolboxPlugin : JavaPlugin(), MctPlayerMoveHandler, Listener {

    private var _component: MctPluginComponent? = null
    private val component: MctPluginComponent get() = _component!!

    override fun onEnable() {
        _component =
            DaggerMctPluginComponent.builder().plugin(this)
                .build()
        logger.info("Hello world")
        server.pluginManager.registerEvents(this, this)
        getCommand("mct compass")?.setSuspendingExecutor(Compass2())
        server.helpMap.helpTopics
    }

    override fun onDisable() {
        logger.info("Goodbye world")
        _component = null
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false
        val mctNode =
            component.rootNodes()[command.name.toLowerCase(Locale.ENGLISH)]
                ?: return false
        val context = CommandContext(
            sender = sender,
            command = command,
            label = label,
            args = args,
            origArgs = args,
            depth = 0,
            plugin = this
        )
        mctNode.handleCommand(context)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        val mctNode =
            component.rootNodes()[command.name.toLowerCase(Locale.ENGLISH)]
                ?: return null
        val context = TabCompleteContext(
            sender = sender,
            command = command,
            alias = alias,
            args = args,
            origArgs = args,
            depth = 0,
            plugin = this
        )
        return mctNode.handleTabComplete(context)
    }

    @EventHandler
    override fun onPlayerMove(event: PlayerMoveEvent) {
        component.playerMoveHandlers().forEach { it.onPlayerMove(event) }
    }
}
