package ca.allanwang.minecraft.toolbox.base

import com.github.shynixn.mccoroutine.launch
import java.util.*
import javax.inject.Qualifier
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PluginScope

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RootNode

/**
 * Base node for handling plugin interactions.
 * Nodes are singletons with respect to the plugin
 */
@PluginScope
abstract class MctNode(val name: String) {

    var helpArgs: String? = null

    var help: String? = null

    private val _children: MutableMap<String, MctNode> = sortedMapOf()

    val children: Map<String, MctNode> get() = _children

    protected fun children(vararg nodes: MctNode) {
        _children.putAll(nodes.associateBy { it.name.toLowerCase(Locale.ENGLISH) })
    }

    fun handleCommand(context: CommandContext): Boolean {
        val key = context.args.firstOrNull()?.toLowerCase(Locale.ENGLISH)
            ?: return false
        val child = _children[key]
        if (child != null) {
            return child.handleCommand(context.child())
        }
        context.plugin.launch {
            context.command()
        }
        // If node isn't root node, we'll consume the command
        return context.depth > 0
    }

    fun handleTabComplete(context: TabCompleteContext): List<String>? {
        val key = context.args.firstOrNull()?.toLowerCase(Locale.ENGLISH)
            ?: return null
        val child = _children[key]
        if (child != null) {
            return child.handleTabComplete(context.child())
        }
        return context.tabComplete()
    }

    protected open suspend fun CommandContext.command() = Unit

    protected open fun TabCompleteContext.tabComplete(): List<String>? {
        val prefix = args.firstOrNull()
        val candidates =
            _children.keys.takeIf { it.isNotEmpty() }?.toList() ?: return null
        return if (prefix.isNullOrEmpty()) candidates else candidates.filter {
            it.startsWith(
                prefix,
                ignoreCase = true
            )
        }
    }

}