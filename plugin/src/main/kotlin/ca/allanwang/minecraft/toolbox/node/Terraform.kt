package ca.allanwang.minecraft.toolbox.node

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.base.TabCompleteContext
import ca.allanwang.minecraft.toolbox.base.blockBelow
import ca.allanwang.minecraft.toolbox.helper.TerraformHelper
import org.bukkit.Material
import org.bukkit.block.Block
import java.awt.Point
import javax.inject.Inject

@PluginScope
class Terraform @Inject internal constructor(
    up: Up, down: Down, fill: Fill
) : MctNode(name = "terraform") {

    init {
        children(up, down, fill)
    }

    @PluginScope
    class Up @Inject internal constructor(
        private val terraformHelper: TerraformHelper
    ) : MctNode(name = "up") {
        override val help: String =
            "Remove blocks above path up to specified height."

        override fun TabCompleteContext.tabComplete(): List<String> =
            listOf("~")

        override suspend fun CommandContext.command() {
            innerPointSequence(up = false, terraformHelper = terraformHelper) {
                it.type = Material.AIR
            }
        }
    }

    @PluginScope
    class Down @Inject internal constructor(
        private val terraformHelper: TerraformHelper
    ) : MctNode(name = "down") {
        override val help: String =
            "Remove blocks below path down to specified depth."

        override fun TabCompleteContext.tabComplete(): List<String> =
            listOf("~")

        override suspend fun CommandContext.command() {
            innerPointSequence(up = false, terraformHelper = terraformHelper) {
                it.type = Material.AIR
            }
        }
    }

    @PluginScope
    class Fill @Inject internal constructor(
        private val terraformHelper: TerraformHelper
    ) : MctNode(name = "fill") {
        override val help: String =
            "Fill blocks inside path down to specified depth."

        override fun TabCompleteContext.tabComplete(): List<String> =
            listOf("~")

        override suspend fun CommandContext.command() {
            innerPointSequence(up = false, terraformHelper = terraformHelper) {
                it.type = Material.DIRT
            }
        }
    }
}

private const val MAX_HEIGHT = 50
private const val MAX_PATH_SIZE = 100

private suspend fun CommandContext.height(name: String = "height"): Int {
    val height = args.firstOrNull()?.toIntOrNull()
        ?: fail("Please supply $name (max $MAX_HEIGHT)")
    if (height <= 0 || height > MAX_HEIGHT) fail(
        "$name must be positive; max $MAX_HEIGHT"
    )
    return height
}

private suspend fun CommandContext.innerPathPoints(terraformHelper: TerraformHelper): List<Point> {
    val startBlock = sender.blockBelow
    logger.info { "On ${startBlock.type}" }
    return terraformHelper.pointsInPolygon(startBlock, maxSize = MAX_PATH_SIZE)
        ?: fail(buildString {
            append("Not standing on valid path. ")
            append("Make sure blocks form a polygon, and that each block in the path is adjacent to exactly 2 blocks. ")
            append("Max path size is $MAX_PATH_SIZE. ")
        })
}

private suspend fun CommandContext.innerPointSequence(
    up: Boolean,
    terraformHelper: TerraformHelper,
    action: (Block) -> Unit
) {
    val height = height(if (up) "height" else "depth")
    val points = innerPathPoints(terraformHelper)
    val world = sender.world

    // Range starts at -2 because ground level is below the path, which is below the player.
    val yRange =
        if (up) ((sender.location.blockY - 2)..(sender.location.blockY - 2 + height)) else ((sender.location.blockY - 2) downTo (sender.location.blockY - 2 - height))
    yRange.forEach { y ->
        points.forEach { p ->
            val block = world.getBlockAt(p.x, y, p.y)
            action(block)
        }
    }
}