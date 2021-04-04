package ca.allanwang.minecraft.toolbox.helper

import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.core.PointsInPolygon
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.awt.Point
import javax.inject.Inject

@PluginScope
class TerraformHelper @Inject internal constructor(

) {

    private enum class BlockFace2D(val blockFace: BlockFace) {
        North(BlockFace.NORTH),
        South(BlockFace.SOUTH),
        East(BlockFace.EAST),
        West(BlockFace.WEST)
    }

    /**
     * Restrict block sequence such that all blocks are within [maxSize] spaces from each other.
     * Updates are sent through [boundingBox], which should be centered around the initial block.
     */
    private fun Sequence<Block>.withinBounds(
        boundingBox: PointsInPolygon.BoundingBox,
        maxSize: Int
    ): Sequence<Block> = sequence {
        for (element in this@withinBounds) {
            boundingBox.bind(element.x, element.z)
            if (boundingBox.maxSize <= maxSize) {
                yield(element)
            }
        }
    }

    private inline fun <T, K, V> Iterable<T>.groupBySet(
        keySelector: (T) -> K,
        valueTransform: (T) -> V
    ): Map<K, Set<V>> {
        return groupBySetTo(LinkedHashMap(), keySelector, valueTransform)
    }

    /**
     * Copy of groupBy, where list is replaced by set.
     */
    private inline fun <T, K, V, M : MutableMap<in K, MutableSet<V>>> Iterable<T>.groupBySetTo(
        destination: M,
        keySelector: (T) -> K,
        valueTransform: (T) -> V
    ): M {
        for (element in this) {
            val key = keySelector(element)
            val set = destination.getOrPut(key) { mutableSetOf() }
            set.add(valueTransform(element))
        }
        return destination
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
        val boundingBox = PointsInPolygon.BoundingBox(block.x, block.z)

        val path =
            generateSequence<Pair<Block, BlockFace2D?>>(block to null) { (block, blockFace) ->
                val candidates = blockFaces2D.mapNotNull {
                    if (it == blockFace) return@mapNotNull null
                    val next = block.getRelative(it.blockFace)
                    if (next.isEmpty) null else next to it
                }
                if (candidates.size != 1) null else candidates.first()
            }
                .drop(1)
                .map { it.first }
                .withinBounds(boundingBox, maxSize)
                .takeWhile { it != block }
                .toList()

        // Check if path is complete
        if (path.lastOrNull()?.getFace(block) == null) return null
        val fullPath = (path + block).map { Point(it.x, it.z) }
        val pointsInPolygon =
            PointsInPolygon(path = fullPath, boundingBox = boundingBox)
        TODO()
    }

}
