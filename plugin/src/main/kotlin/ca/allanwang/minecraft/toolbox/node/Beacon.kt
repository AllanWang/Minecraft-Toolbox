package ca.allanwang.minecraft.toolbox.node

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.db.BeaconTable
import ca.allanwang.minecraft.toolbox.db.mctTransaction
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import java.util.logging.Logger
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
        private val beaconTable: BeaconTable
    ) : MctNode(name = "list") {

        override suspend fun CommandContext.command() {
            logger.info { "beacon list start" }
            val beacons = mctTransaction {
                beaconTable.selectAll().orderBy(
                    beaconTable.x to SortOrder.ASC,
                    beaconTable.y to SortOrder.ASC,
                    beaconTable.z to SortOrder.ASC
                ).mapIndexed { index, it ->
                    buildString {
                        append(index)
                        append(". ")
                        append("[")
                        append(it[beaconTable.x])
                        append(",")
                        append(it[beaconTable.y])
                        append(",")
                        append(it[beaconTable.z])
                        append("]")
                        server.getPlayer(it[beaconTable.user])?.let { player ->
                            append("\t")
                            append(player.name)
                        }
                    }
                }
            }
            echo(
                if (beacons.isEmpty()) "No beacons found" else beacons.joinToString(
                    "\n"
                )
            )
        }
    }
}