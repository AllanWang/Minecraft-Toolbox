package ca.allanwang.minecraft.toolbox.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

class BukkitCoroutineDispatcher(private val plugin: Plugin) :
    CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.getServer().isPrimaryThread) {
            block.run()
        } else {
            Bukkit.getServer().scheduler.runTask(plugin, block)
        }
    }
}
