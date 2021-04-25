package ca.allanwang.minecraft.toolbox.base

import kotlinx.coroutines.CancellationException
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.logging.Logger

interface NodeContext<T : NodeContext<T>> {
    val sender: CommandSender
    val args: Array<out String>
    val origArgs: Array<out String>
    val depth: Int
    val plugin: Plugin

    val server: Server get() = plugin.server
    val logger: Logger get() = plugin.logger

    fun child(): NodeContext<T>

    suspend fun fail(text: String): Nothing {
        echo(text)
        throw CancellationException(text)
    }

    fun echo(text: String) = sender.sendMessage(text)

    fun String?.nameToPlayerOrNull(): Player? =
        if (this != null) server.getPlayer(this) else null

    suspend fun String?.nameToPlayer(): Player = nameToPlayerOrNull()
        ?: fail("Player $this does not exist")
}

class CommandContext(
    override val sender: Player,
    val command: Command,
    val label: String,
    override val args: Array<out String>,
    override val origArgs: Array<out String>,
    override val depth: Int,
    override val plugin: Plugin,
) : NodeContext<CommandContext> {

    override fun child(): CommandContext = subDepth(depth + 1)

    fun subDepth(newDepth: Int): CommandContext = CommandContext(
        sender = sender,
        command = command,
        label = label,
        args = origArgs.sliceArray(newDepth..origArgs.lastIndex),
        origArgs = origArgs,
        depth = newDepth,
        plugin = plugin,
    )
}

class TabCompleteContext(
    override val sender: CommandSender,
    val command: Command,
    val alias: String,
    override val args: Array<out String>,
    override val origArgs: Array<out String>,
    override val depth: Int,
    override val plugin: Plugin,
) : NodeContext<TabCompleteContext> {

    override fun child(): TabCompleteContext = TabCompleteContext(
        sender = sender,
        command = command,
        alias = alias,
        args = origArgs.sliceArray(depth + 1..origArgs.lastIndex),
        origArgs = origArgs,
        depth = depth + 1,
        plugin = plugin,
    )
}