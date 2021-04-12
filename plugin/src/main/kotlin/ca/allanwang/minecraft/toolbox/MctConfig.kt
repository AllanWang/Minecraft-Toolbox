package ca.allanwang.minecraft.toolbox

import org.bukkit.configuration.file.FileConfiguration

interface MctConfig {
    val sqlLogging: Boolean
    val sqlPrefix: String
    val sqlUrl: String
    val sqlDriver: String
    val sqlUsername: String
    val sqlPassword: String
}

class MctFileConfig(private val config: FileConfiguration) : MctConfig {

    private fun string(key: String, default: String? = null): String =
        config.getString(key, default)!!

    private fun boolean(key: String, default: Boolean = false): Boolean =
        config.getBoolean(key, default)

    override val sqlLogging: Boolean = boolean("sql-logging")
    override val sqlPrefix: String = string("sql-prefix")
    val sqlHost: String = string("sql-host")
    val sqlPort: String = string("sql-port")
    val sqlDatabase: String = string("sql-database")

    override val sqlUrl: String = buildString {
        append("jdbc:mysql://")
        append(sqlHost)
        append(":")
        append(sqlPort)
        append("/")
        append(sqlDatabase)
        append("?useSSL=false")
        append("&allowPublicKeyRetrieval=true")
//        append("&useUnicode=yes")
//        append("&characterEncoding=UTF-8")
    }

    override val sqlDriver: String = "com.mysql.jdbc.Driver"
    override val sqlUsername: String = string("sql-username")
    override val sqlPassword: String = string("sql-password")

}
