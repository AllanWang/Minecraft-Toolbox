package ca.allanwang.minecraft.toolbox.helper

import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.core.BoundingBox
import ca.allanwang.minecraft.toolbox.core.PointKt
import ca.allanwang.minecraft.toolbox.core.PolygonData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.awt.Point
import java.util.logging.Logger
import javax.inject.Inject

@PluginScope
class TerraformHelper @Inject internal constructor(
    private val logger: Logger
) {

    private enum class BlockFace2D(
        val blockFace: BlockFace,
        val oppositeBlockFace: BlockFace
    ) {
        North(BlockFace.NORTH, BlockFace.SOUTH),
        South(BlockFace.SOUTH, BlockFace.NORTH),
        East(BlockFace.EAST, BlockFace.WEST),
        West(BlockFace.WEST, BlockFace.EAST)
    }

    /**
     * Restrict block sequence such that all blocks are within [maxSize] spaces from each other.
     * Updates are sent through [boundingBox], which should be centered around the initial block.
     */
    private fun Sequence<Block>.withinBounds(
        boundingBox: BoundingBox,
        maxSize: Int
    ): Sequence<Block> = sequence {
        for (element in this@withinBounds) {
            boundingBox.bind(element.x, element.z)
            if (boundingBox.maxSize <= maxSize) {
                yield(element)
            }
        }
    }

    /**
     * Gets a list of points within a 2D path.
     *
     * A path is a closed loop of consecutive blocks,
     * where all blocks in the path are on the same y (elevation) plane,
     * and where all blocks are adjacent to exactly two other blocks in the x, z plane.
     * This means that segments in the polygon cannot intersect or touch, making the shape unambiguous.
     *
     * [maxSize] represents the maximum side for a bounding box.
     * Path searching is cancelled if [maxSize] is surpassed.
     *
     * Returns a list of points in the polygon if it exists, or null if the block is not part of a path defined above.
     */
    suspend fun pointsInPolygon(
        block: Block,
        maxSize: Int = 100
    ): List<Point>? {
        val blockFaces2D = BlockFace2D.values()
        val boundingBox = BoundingBox(block.x, block.z)

        val path =
            generateSequence<Pair<Block, BlockFace2D?>>(block to null) { (block, blockFace2D) ->
                val candidates = blockFaces2D.mapNotNull {
                    // Ignore candidate that would map back to previous block
                    if (it.blockFace == blockFace2D?.oppositeBlockFace) return@mapNotNull null
                    val next = block.getRelative(it.blockFace)
                    if (next.isEmpty) null else next to it
                }
                // First block will have 2 valid faces; subsequent will only have one.
                if (blockFace2D == null && candidates.size != 2) return@generateSequence null
                if (blockFace2D != null && candidates.size != 1) return@generateSequence null
                candidates.first()
            }
                .drop(1)
                .map { it.first }
                .withinBounds(boundingBox, maxSize)
                .takeWhile { it != block }
                .toList()

        // Check if path is complete
        if (path.size < 8 || path.lastOrNull()
                ?.getFace(block) == null
        ) return null

        logger.info { "Created path" }

        return withContext(Dispatchers.IO) {
            val fullPath = (path + block).map { PointKt(it.x, it.z) }
            val pointsInPolygon =
                PolygonData(path = fullPath, boundingBox = boundingBox)

            pointsInPolygon.pointsInPolygon().map { Point(it.x, it.y) }
        }
    }

}
