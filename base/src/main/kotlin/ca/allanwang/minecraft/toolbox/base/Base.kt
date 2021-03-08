package ca.allanwang.minecraft.toolbox.base

import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable
import org.bukkit.plugin.Plugin

inline fun <reified T> Metadatable.metadata(key: String, plugin: Plugin): T? =
    getMetadata(key).firstOrNull { it.owningPlugin == plugin }
        ?.value() as? T?

fun <T> Metadatable.metadata(key: String, plugin: Plugin, value: T?) {
    setMetadata(key, FixedMetadataValue(plugin, value))
}