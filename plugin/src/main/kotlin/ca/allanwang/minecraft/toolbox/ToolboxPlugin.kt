@file:JvmName("ToolbarPlugin")

package ca.allanwang.minecraft.toolbox

import ca.allanwang.minecraft.toolbox.base.BukkitCoroutineDispatcher
import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.Mct
import ca.allanwang.minecraft.toolbox.base.TabCompleteContext
import ca.allanwang.minecraft.toolbox.base.toLowerCaseMct
import ca.allanwang.minecraft.toolbox.sqldelight.MctDb
import com.mysql.cj.jdbc.MysqlDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class ToolboxPlugin : JavaPlugin() {

    private var _component: MctPluginComponent? = null
    private val component: MctPluginComponent get() = _component!!

    override fun onEnable() {
        initConfig()
        val mctConfig = try {
            MctFileConfig(config)
        } catch (e: NullPointerException) {
            throw IllegalStateException("Missing config")
        }
        val dataSource = try {
            MysqlDataSource().apply {
                setURL(mctConfig.sqlUrl)
                user = mctConfig.sqlUsername
                password = mctConfig.sqlPassword
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, e) { "Failed to create data source" }
            throw IllegalStateException("Missing data source")
        }
        val mct = object : Mct {
            override val mctLogger: Logger = logger

            override val mctScope: CoroutineScope = CoroutineScope(
                BukkitCoroutineDispatcher(this@ToolboxPlugin)
            )

            val eventFlow: MutableSharedFlow<Event> =
                MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)

            override val events: SharedFlow<Event> get() = eventFlow
        }
        _component =
            DaggerMctPluginComponent.builder()
                .plugin(this)
                .config(mctConfig)
                .mct(mct)
                .dataSource(dataSource)
                .build()
        val mctEventHandler =
            MctEventHandler(mct = mct, eventFlow = mct.eventFlow)
        MctDb.Schema.create(component.jdbcDriver())
        component.rootNodes() // Init everything
        logger.info("Hello world")
        server.pluginManager.registerEvents(mctEventHandler, this)
    }

    private fun initConfig() {
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.isFile) {
            val defaultConfigStream =
                ToolboxPlugin::class.java.classLoader.getResourceAsStream("default_config.yml")
                    ?: throw IllegalStateException("Missing default configs")
            configFile.parentFile?.mkdirs()
            defaultConfigStream.use { input ->
                configFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    override fun onDisable() {
        logger.info("Goodbye world")
        Bukkit.getServer().scheduler.cancelTasks(this)
        _component ?: return
        component.mct().mctScope.cancel()
        component.dataSource().connection.close()
        _component = null
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false
        val mctNode =
            component.rootNodes()[command.name.toLowerCaseMct()]
                ?: return false
        val context = CommandContext(
            sender = sender,
            command = command,
            label = label,
            args = args,
            origArgs = args,
            depth = 0,
            plugin = this
        )
        component.mct().mctScope.launch {
            mctNode.handleCommand(context)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        val mctNode =
            component.rootNodes()[command.name.toLowerCaseMct()]
                ?: return null
        val context = TabCompleteContext(
            sender = sender,
            command = command,
            alias = alias,
            args = args,
            origArgs = args,
            depth = 0,
            plugin = this
        )
        val result = mctNode.handleTabComplete(context)
//        logger.info { "tab ${command.name} ${args.contentToString()} -> $result" }
        return result
    }
}
