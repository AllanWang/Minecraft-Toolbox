package ca.allanwang.minecraft.toolbox.node

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.helper.numberedList
import ca.allanwang.minecraft.toolbox.helper.prettyJoin
import ca.allanwang.minecraft.toolbox.sqldelight.MctDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@PluginScope
class Beacon @Inject internal constructor(
    list: List
) : MctNode(name = "beacon") {

    init {
        children(list)
    }

    @PluginScope
    class List @Inject internal constructor(
        private val mctDb: MctDb
    ) : MctNode(name = "list") {

        override suspend fun CommandContext.command() {
            logger.info { "beacon list start" }
            val beacons = withContext(Dispatchers.IO) {
                mctDb.mctDbQueries.selectAll { _, user, x, y, z ->
                    "[$x, $y, $z]" to server.getPlayer(UUID.fromString(user))?.name
                }.executeAsList().prettyJoin(minSpace = 2).numberedList()
            }
            echo(
                if (beacons.isEmpty()) "No beacons found"
                else buildString {
                    appendLine("Beacons")
                    appendLine(beacons.joinToString("\n"))
                }
            )
        }
    }
}