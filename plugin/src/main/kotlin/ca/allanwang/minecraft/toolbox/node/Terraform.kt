package ca.allanwang.minecraft.toolbox.node

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.base.TabCompleteContext
import ca.allanwang.minecraft.toolbox.base.blockBelow
import ca.allanwang.minecraft.toolbox.core.BoundingPrism
import ca.allanwang.minecraft.toolbox.core.max
import ca.allanwang.minecraft.toolbox.core.untilFirstNull
import ca.allanwang.minecraft.toolbox.helper.TerraformHelper
import org.bukkit.Material
import org.bukkit.block.Block
import java.awt.Point
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

@PluginScope
class Terraform @Inject internal constructor(
    up: Up, down: Down, fill: Fill, clear: Clear
) : MctNode(name = "terraform") {

    init {
        children(up, down, fill, clear)
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
            innerPointSequencePath(
                up = true,
                terraformHelper = terraformHelper
            ) {
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
            innerPointSequencePath(
                up = false,
                terraformHelper = terraformHelper
            ) {
                it.type = Material.AIR
            }
        }
    }

    @PluginScope
    class Fill @Inject internal constructor(
        private val terraformHelper: TerraformHelper
    ) : MctNode(name = "fill") {
        override val help: String =
            "Fill blocks inside path down to specified depth, or with provided coordinates"

        override fun TabCompleteContext.tabComplete(): List<String> =
            listOf("~", "x1,y1,z1 x2,y2,z2 ...")

        override suspend fun CommandContext.command() {
            innerPointSequence(up = false, terraformHelper = terraformHelper) {
                it.type = Material.DIRT
            }
        }
    }

    @PluginScope
    class Clear @Inject internal constructor(
    ) : MctNode(name = "clear") {
        override val help: String =
            "Fill blocks inside provided coordinates"

        override fun TabCompleteContext.tabComplete(): List<String> =
            listOf("x1,y1,z1 x2,y2,z2 ...")

        override suspend fun CommandContext.command() {
            innerPointSequencePrism {
                it.type = Material.AIR
            }
        }
    }
}

private const val MAX_HEIGHT = 50
private const val MAX_PATH_SIZE = 100

// Max distance from player to terraform location
private const val TERRAFORM_MAX_REMOTE_DISTANCE = 50

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

/**
 * Collect points in a 3D prism by providing a list of xyz points
 */
private suspend fun CommandContext.prismPoints(): BoundingPrism {
    val numbers = sequence {
        args.forEach { arg ->
            arg.split(",").forEach {
                yield(it.trim().toIntOrNull())
            }
        }
    }.untilFirstNull().toList()

    if (numbers.size % 3 != 0) {
        fail("Points must be provided in groups of 3 (x, y, z)")
    }

    val points = numbers.chunked(3).map { (x, y, z) -> Triple(x, y, z) }

    if (points.size < 2) {
        fail("Must have at least 2 points; received ${points.size}")
    }

    val (firstX, firstY, firstZ) = points.first()
    val boundingPrism = BoundingPrism(firstX, firstY, firstZ)
    points.forEach { (x, y, z) -> boundingPrism.bind(x, y, z) }
    return boundingPrism
}

/**
 * Terraform based on path that player is standing on
 */
private suspend fun CommandContext.innerPointSequencePath(
    up: Boolean,
    terraformHelper: TerraformHelper,
    action: (Block) -> Unit
) {
    val height = height(if (up) "height" else "depth")
    val points = innerPathPoints(terraformHelper)
    val world = sender.world
    // Range starts at -2 because ground level is below the path, which is below the player.
    val yRange =
        if (up) ((sender.location.blockY - 2)..(sender.location.blockY - 2 + height))
        else ((sender.location.blockY - 2) downTo (sender.location.blockY - 2 - height))

    echo("Terraforming ${points.size * (yRange.last - yRange.first)} blocks")

    yRange.forEach { y ->
        points.forEach { p ->
            val block = world.getBlockAt(p.x, y, p.y)
            action(block)
        }
    }
}

/**
 * Terraform based on provided points
 */
private suspend fun CommandContext.innerPointSequencePrism(action: (Block) -> Unit) {
    val boundingPrism = prismPoints()
    val world = sender.world

    fun minAbs(ref: Int, min: Int, max: Int) =
        min(abs(ref - min), abs(ref - max))

    val distanceToPrism = max(
        minAbs(
            sender.location.blockX,
            boundingPrism.minX,
            boundingPrism.maxX
        ),
        minAbs(
            sender.location.blockY,
            boundingPrism.minY,
            boundingPrism.maxY
        ),
        minAbs(
            sender.location.blockZ,
            boundingPrism.minZ,
            boundingPrism.maxZ
        ),
    )

    if (distanceToPrism > TERRAFORM_MAX_REMOTE_DISTANCE) {
        fail("You are too far away from the terraform location. Please move within $TERRAFORM_MAX_REMOTE_DISTANCE blocks")
    }

    echo("Terraforming ${boundingPrism.sizeX * boundingPrism.sizeY * boundingPrism.sizeZ} blocks")

    (boundingPrism.minX..boundingPrism.maxX).forEach { x ->
        (boundingPrism.minY..boundingPrism.maxY).forEach { y ->
            (boundingPrism.minZ..boundingPrism.maxZ).forEach { z ->
                val block = world.getBlockAt(x, y, z)
                action(block)
            }
        }
    }
}

private suspend fun CommandContext.innerPointSequence(
    up: Boolean,
    terraformHelper: TerraformHelper,
    action: (Block) -> Unit
) {

    if (args.size < 3 && args.firstOrNull()?.toIntOrNull() != null)
        innerPointSequencePath(up, terraformHelper, action)
    else
        innerPointSequencePrism(action)
}