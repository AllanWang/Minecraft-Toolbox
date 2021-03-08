package ca.allanwang.minecraft.toolbox

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktConsole
import com.github.ajalt.clikt.output.HelpFormatter
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable
import org.bukkit.plugin.Plugin
import java.util.logging.Logger

interface MctContext : MctPluginContext {
    val sender: Player
    val command: Command
    val label: String
    val args: Array<out String>
}

class MctContextImpl(
    override val sender: Player,
    override val command: Command,
    override val label: String,
    override val args: Array<out String>,
    val pluginContext: MctPluginContext,
) : MctContext, MctPluginContext by pluginContext

interface MctPluginContext {
    val logger: Logger
    val plugin: Plugin
    val server: Server
}

class MctPluginContextImpl(override val plugin: Plugin) : MctPluginContext {
    override val logger: Logger
        get() = plugin.logger
    override val server: Server
        get() = plugin.server
}

abstract class MctCommand : CliktCommand() {

    var subcommands: List<MctCommand> = emptyList()
        private set

    override fun run() = Unit

    fun subcommands(vararg commands: MctCommand) {
        cliktSubcommands(commands)
        subcommands = subcommands + commands.toList()
    }

    // Alias to match extension type
    private fun cliktSubcommands(commands: Array<out CliktCommand>) {
        subcommands(*commands)
    }

    /**
     * Copy of [main] without exitProcess
     */
    fun mct(argv: List<String>) {
        try {
            parse(argv)
        } catch (e: ProgramResult) {
        } catch (e: PrintHelpMessage) {
            echo(e.command.getFormattedHelp())
        } catch (e: PrintCompletionMessage) {
            val s =
                if (e.forceUnixLineEndings) "\n" else currentContext.console.lineSeparator
            echo(e.message, lineSeparator = s)
        } catch (e: PrintMessage) {
            echo(e.message)
        } catch (e: UsageError) {
            echo(e.helpMessage(), err = true)
        } catch (e: CliktError) {
            echo(e.message, err = true)
        } catch (e: Abort) {
            echo(currentContext.localization.aborted(), err = true)
        }
    }
}

class MctConsole(val player: Player) : CliktConsole {
    override val lineSeparator: String = "\n"

    override fun print(text: String, error: Boolean) {
        player.sendMessage(text)
    }

    override fun promptForLine(prompt: String, hideInput: Boolean): String? {
        return null
    }

}

class MctPrompt(val text: String) : StringPrompt() {
    override fun getPromptText(context: ConversationContext): String {
        return text
    }

    override fun acceptInput(
        context: ConversationContext,
        input: String?
    ): Prompt? {
        return null
    }

}

class MctHelp : HelpFormatter {
    override fun formatHelp(
        prolog: String,
        epilog: String,
        parameters: List<HelpFormatter.ParameterHelp>,
        programName: String
    ): String {
        TODO("Not yet implemented")
    }

    override fun formatUsage(
        parameters: List<HelpFormatter.ParameterHelp>,
        programName: String
    ): String {
        TODO("Not yet implemented")
    }

}

interface MctPlayerMoveHandler {
    fun onPlayerMove(event: PlayerMoveEvent)
}

inline fun <reified T> Metadatable.metadata(key: String, plugin: Plugin): T? =
    getMetadata(key).firstOrNull { it.owningPlugin == plugin }
        ?.value() as? T?

fun <T> Metadatable.metadata(key: String, plugin: Plugin, value: T?) {
    setMetadata(key, FixedMetadataValue(plugin, value))
}