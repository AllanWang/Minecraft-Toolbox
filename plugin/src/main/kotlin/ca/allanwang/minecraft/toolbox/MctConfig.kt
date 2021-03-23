package ca.allanwang.minecraft.toolbox

import org.bukkit.configuration.file.FileConfiguration

class MctConfig(private val config: FileConfiguration) {

    private fun string(key: String): String = config.getString(key)!!

    val sqlPrefix: String = string("sql-prefix")
    val sqlHost: String = string("sql-host")
    val sqlPort: String = string("sql-port")
    val sqlDatabase: String = string("sql-database")
    val sqlUsername: String = string("sql-username")
    val sqlPassword: String = string("sql-password")

}