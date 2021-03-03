@file:JvmName("ToolboxPlugin")

package ca.allanwang.minecraft.toolbox

import org.bukkit.plugin.java.JavaPlugin

class ToolboxPlugin : JavaPlugin() {
    override fun onEnable() {
       logger.info("Hello world")
    }

    override fun onDisable() {

    }
}