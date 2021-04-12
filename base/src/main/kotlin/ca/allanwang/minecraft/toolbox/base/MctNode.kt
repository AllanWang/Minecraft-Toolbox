package ca.allanwang.minecraft.toolbox.base

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
abstract class MctNode(
    val name: String,
    val aliases: Set<String> = emptySet()
) {

    open val helpArgs: String? = null

    open val help: String? = null

    private val _children: MutableMap<String, MctNode> = mutableMapOf()

    /**
     * Lower case child names (no aliases)
     */
    private val childNames: MutableSet<String> = sortedSetOf()

    val children: Map<String, MctNode> get() = _children

    protected fun children(vararg nodes: MctNode) {
        nodes.forEach { node ->
            val key = node.name.toLowerCaseMct()
            childNames.add(key)
            _children[key] = node
            node.aliases.map { it.toLowerCaseMct() }
                .forEach { alias ->
                    _children[alias] = node
                }
        }
    }


    suspend fun handleCommand(context: CommandContext): Boolean {
        val key = context.args.firstOrNull()?.toLowerCaseMct()
        if (key == null) {
            context.command()
            return false
        }
        val child = _children[key]
        if (child != null) {
            return child.handleCommand(context.child())
        }
        context.command()
        // If node isn't root node, we'll consume the command
        return context.depth > 0
    }

    fun handleTabComplete(context: TabCompleteContext): List<String>? {
        val key = context.args.firstOrNull()?.toLowerCaseMct()
            ?: return null
        val child = _children[key]
        if (child != null) {
            return child.handleTabComplete(context.child())
        }
        return context.tabComplete()
    }

    protected open suspend fun CommandContext.command() = Unit

    protected open fun TabCompleteContext.tabComplete(): List<String>? =
        defaultTabComplete()

    protected fun TabCompleteContext.defaultTabComplete(): List<String>? {
        val prefix = args.firstOrNull()
        val candidates =
            childNames.takeIf { it.isNotEmpty() }?.toList() ?: return null
        return if (prefix.isNullOrEmpty()) candidates else candidates.filter {
            it.startsWith(
                prefix,
                ignoreCase = true
            )
        }
    }

}