package ca.allanwang.minecraft.toolbox.core

import java.awt.Point
import kotlin.math.max
import kotlin.math.min

internal class PointsInPolygon(
    val path: List<Point>,
    val boundingBox: BoundingBox
) {

    internal data class BoundingBox(
        var minX: Int,
        var minY: Int,
        var maxX: Int = minX,
        var maxY: Int = minY
    ) {
        fun bind(x: Int, y: Int) {
            minX = min(minX, x)
            maxX = max(maxX, x)
            minY = min(minY, y)
            maxY = max(maxY, y)
        }

        inline val sizeX: Int get() = maxX - minX
        inline val sizeY: Int get() = maxY - minY

        val maxSize: Int get() = max(sizeX, sizeY)
    }

    internal data class PointState(
        val inside: Boolean,
        val edge: Boolean
    )

    internal class PolygonStatus(val sizeX: Int, val sizeY: Int) {
        internal val data: Array<Array<PointState?>> =
            Array(sizeX) { Array(sizeY) { null } }

        operator fun get(point: Point?): PointState? =
            if (point == null) null
            else data[point.x][point.y]

        operator fun set(point: Point, state: PointState?) {
            data[point.x][point.y] = state
        }
    }

    // Normalize path for now
    private val offsetPath =
        path.map { Point(it.x - boundingBox.minX, it.y - boundingBox.minY) }

    private val coordGraph = offsetPath.groupBySet({ it.x }, { it.y })

    private fun isEdge(point: Point?) =
        if (point == null) false else isEdge(point.x, point.y)

    private fun isEdge(x: Int, y: Int) = coordGraph[x]?.contains(y) == true

    private val polygonStatus =
        PolygonStatus(sizeX = boundingBox.sizeX, sizeY = boundingBox.sizeY)

    private val pointsInPolygon: Lazy<List<Point>> = lazy {
        resolve()
        extract()
    }

    /**
     * Added a function to indicate that computation can be expensive
     */
    fun pointsInPolygon(): List<Point> = pointsInPolygon.value

    /**
     * The algorithm uses ray casting, with some changes for Minecraft's 2D grid system.
     * We define a point as within the polygon if a ray from the point to any direction intersects an odd number of edges.
     * Unfortunately, there are many cases where edges will align with the ray, so we'd need to fallback with a perpendicular ray instead.
     * Given the constraints of the path, each path point will only have neighbouring path points on one of the axes.
     * We wish to find the status of all points in the [boundingBox], so we can improve our runtime by computing all points along
     * a ray, with a ray coming from the edge of the bounding box. This could be done with sequences, but since we have a fallback ray cast,
     * we need to use dynamic programming to retain information. For points in the edge, we define them as "inside" the polygon
     * if the nearest non edge point down or to the right of the point is inside.
     * In other words, the true edge is effectively along the top and left of the edge block.
     * This is unambiguous due to our restrictions for paths, where edges cannot come into contact with another part of the polygon.
     *
     * A pure data variant of finding points in polygon.
     *
     * [path] is a list of points within [boundingBox], forming a polygon as defined in [pointsInPolygon].
     * [boundingBox] is a grid constraint when computing points.
     *
     * Returns list of points within polygon.
     *
     */
    private fun resolve() {
        (0..boundingBox.sizeY).forEach { y -> resolveRow(y) }
    }

    private fun resolveRow(y: Int) {
        val points = (0..polygonStatus.sizeX).map { x -> Point(x, y) }
        val fallbackResolve = { point: Point ->
            resolveCol(point.x)
        }
        val fallbackFailed = { point: Point ->
            throw IllegalStateException("Edge along row at ${point.coord()} not resolved")
        }
        resolveAxis(points, fallbackResolve, fallbackFailed)
    }

    /**
     * Copy of [resolveRow] with flipped indices for columns
     */
    private fun resolveCol(x: Int) {
        val points = (0..polygonStatus.sizeY).map { y -> Point(x, y) }
        val fallbackResolve = { point: Point ->
            throw IllegalStateException("Failed to fallback resolve col at ${point.coord()}")
        }
        val fallbackFailed = { point: Point ->
            throw IllegalStateException("Edge along col at ${point.coord()} not resolved")
        }
        resolveAxis(points, fallbackResolve, fallbackFailed)
    }

    private fun resolveAxis(
        points: List<Point>,
        fallbackResolve: (Point) -> Unit,
        fallbackFailed: (Point) -> Nothing
    ) {
        val prevState = PointState(inside = false, edge = false)

        var prevInside = prevState.inside

        points.forEachIndexed { index, point ->
            val currState = polygonStatus[point]
            if (currState != null) {
                // already resolved
                prevInside = currState.inside
                return@forEachIndexed
            }
            val isEdge = isEdge(point)
            val isInside: Boolean? = when {
                // Base case
                !isEdge -> prevInside
                // Edge aligned with ray; cannot resolve directly
                isEdge(points.getOrNull(index + 1)) -> null
                // Base case; edge not aligned; flip once
                else -> !prevInside
            }
            prevInside = if (isInside == null) {
                fallbackResolve(point)
                polygonStatus[point]?.inside ?: fallbackFailed(point)
            } else {
                polygonStatus[point] =
                    PointState(inside = isInside, edge = isEdge)
                isInside
            }
        }
    }

    private fun extract(): List<Point> {
        val points = mutableListOf<Point>()
        polygonStatus.data.forEachIndexed { x, nested ->
            nested.forEachIndexed { y, state ->
                if (state?.edge == false && state.inside)
                    points.add(
                        Point(
                            x + boundingBox.minX,
                            y + boundingBox.minY
                        )
                    )
            }
        }
        return points
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

private fun Point.coord(): String = "($x, $y)"