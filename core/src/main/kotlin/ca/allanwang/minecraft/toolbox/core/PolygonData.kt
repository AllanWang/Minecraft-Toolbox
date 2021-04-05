package ca.allanwang.minecraft.toolbox.core

import java.util.logging.Logger
import kotlin.math.max
import kotlin.math.min

data class PolygonData(
    val path: List<PointKt>,
    val boundingBox: BoundingBox
) {

    companion object {
        private val logger = Logger.getLogger("PointsInPolygon")
    }

    data class BoundingBox(
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

        operator fun get(point: PointKt?): PointState? =
            if (point == null) null
            else data[point.x][point.y]

        operator fun set(point: PointKt, state: PointState?) {
            data[point.x][point.y] = state
        }
    }

    // Normalize path for now
    private val offsetPath =
        path.map { PointKt(it.x - boundingBox.minX, it.y - boundingBox.minY) }

    private val coordGraph = offsetPath.groupBySet({ it.x }, { it.y })

    private fun isEdge(point: PointKt?) =
        if (point == null) false else isEdge(point.x, point.y)

    private fun isEdge(x: Int, y: Int) = coordGraph[x]?.contains(y) == true

    internal val polygonStatus =
        PolygonStatus(sizeX = boundingBox.sizeX, sizeY = boundingBox.sizeY)

    private val pointsInPolygon: Lazy<List<PointKt>> = lazy {
        resolve()
        extract()
    }

    /**
     * Added a function to indicate that computation can be expensive
     */
    fun pointsInPolygon(): List<PointKt> = pointsInPolygon.value

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
        (0 until boundingBox.sizeY).forEach { y -> resolveRow(y) }
    }

    private fun resolveRow(y: Int, startX: Int = 0) {
        val fallbackResolve = { point: PointKt ->
            resolveCol(point.x)
            true
        }
        val fallbackFailed = { point: PointKt ->
            throw IllegalStateException("Edge along row at $point not resolved")
        }
        resolveAxis(
            PointKt(startX, y),
            { if (it.x < polygonStatus.sizeX - 1) PointKt(it.x + 1, y) else null },
            fallbackResolve,
            fallbackFailed
        )
    }

    /**
     * Copy of [resolveRow] with flipped indices for columns.
     * This is our fallback call, so further fallback will lead to noop.
     */
    private fun resolveCol(x: Int, startY: Int = 0) {
        val fallbackResolve = { _: PointKt -> false }
        val fallbackFailed = { point: PointKt ->
            throw IllegalStateException("Edge along col at $point not resolved")
        }
        resolveAxis(
            PointKt(x, startY),
            { if (it.y < polygonStatus.sizeY - 1) PointKt(x, it.y + 1) else null },
            fallbackResolve,
            fallbackFailed
        )
    }

    private fun resolveAxis(
        startPoint: PointKt,
        next: (PointKt) -> PointKt?,
        fallbackResolve: (PointKt) -> Boolean,
        fallbackFailed: (PointKt) -> Nothing
    ) {
        val prevState = PointState(inside = false, edge = false)

        var prevInside = prevState.inside

        for (point in generateSequence(startPoint, next)) {
            val currState = polygonStatus[point]
            if (currState != null) {
                // already resolved
                prevInside = currState.inside
                continue
            }
            val isEdge = isEdge(point)
            val isInside: Boolean? = when {
                // Base case
                !isEdge -> prevInside
                // Edge case; first edge is known
                point.x == 0 || point.y == 0 -> true
                // Edge aligned with ray; cannot resolve directly
                isEdge(next(point)) -> null
                // Base case; edge not aligned; flip once
                else -> !prevInside
            }
            prevInside = if (isInside == null) {
                logger.info { "Null previnside $point" }
                if (!fallbackResolve(point)) return
                polygonStatus[point]?.inside ?: fallbackFailed(point)
            } else {
                polygonStatus[point] =
                    PointState(inside = isInside, edge = isEdge)
                isInside
            }
        }
    }

    private fun extract(): List<PointKt> {
        val points = mutableListOf<PointKt>()
        (0 until polygonStatus.sizeY).forEach { y ->
            (0 until polygonStatus.sizeX).forEach { x ->
                val state = polygonStatus.data[x][y]
                if (state?.edge == false && state.inside)
                    points.add(
                        PointKt(
                            x + boundingBox.minX,
                            y + boundingBox.minY
                        )
                    )
            }
        }
        return points
    }
}