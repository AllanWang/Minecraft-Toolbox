package ca.allanwang.minecraft.toolbox

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.CliktConsole
import org.bukkit.command.Command
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import java.util.logging.Logger

class MctContext(
    val logger: Logger,
)

class CommandContext(
    val sender: Player,
    val command: Command,
    val label: String,
    val args: Array<out String>
)

abstract class MctCommand : CliktCommand() {

    abstract val mctContext: MctContext

    final override fun run() {
        mctContext.run()
    }

    open fun MctContext.run() = Unit

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