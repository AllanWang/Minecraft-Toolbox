@file:JvmName("ToolboxPlugin")

package ca.allanwang.minecraft.toolbox

import com.github.ajalt.clikt.core.context
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class ToolboxPlugin : JavaPlugin() {

    override fun onEnable() {
        logger.info("Hello world")
    }

    override fun onDisable() {

    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false
        val mctContext = MctContext(logger = logger)
        val mctCommand = when (command.name.toLowerCase(Locale.US)) {
            "mct" -> Mct(mctContext)
            else -> return false
        }
        mctCommand.context {
            console = MctConsole(sender)
        }
        mctCommand.mct(args.asList())
        return true
    }
}