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

    private val floodFill: FloodFill by lazy { FloodFill() }

    /**
     * Implementation of Flood Fill, or more specifically, Span Fill.
     *
     * https://en.wikipedia.org/wiki/Flood_fill#Span_Filling
     *
     * This is heavily inspired by
     * http://www.adammil.net/blog/v126_A_More_Efficient_Flood_Fill.html
     * and is Kotlinized in this class.
     *
     * For the sake of documentation, we will draw grids where
     * - `o` represents a point inside the polygon,
     * - `.` represents a point outside the polygon (or a point along the path)
     *
     * Points are written as (x, y), where x = point along the row and y = point along the col.
     * The `o` point below is (2, 1).
     * -----------
     * . . . .
     * . . o .
     * . . . .
     */
    private inner class FloodFill {

        /**
         * Shared memory required for algorithm.
         */
        private inner class Context(
            /**
             * Flag grid to indicate visited points.
             * Normalized such that (minX, minY) is (0, 0).
             * Starts with offset path points set to true.
             */
            val flagged: Array<BooleanArray>,
            /**
             * Mutable collection for gathering filled points.
             * Values are _not_ normalized
             */
            val filledPoints: MutableSet<PointKt>,
        ) {
            /**
             * Add denormalized point to collection
             */
            fun addFilledPoint(x: Int, y: Int) {
                filledPoints.add(
                    PointKt(
                        x + boundingBox.minX,
                        y + boundingBox.minY
                    )
                )
            }
        }

        private operator fun Map<Int, Set<Int>>.get(x: Int, y: Int) =
            get(x)?.contains(y) == true

        /**
         * Computed points within the polygon. Points are not offsetted, and are computed via [compute].
         * Result is sorted, and is null prior to computation.
         */
        var filledPoints: Collection<PointKt>? = null

        private operator fun Array<BooleanArray>.get(x: Int, y: Int) =
            this[x][y]

        private operator fun Array<BooleanArray>.set(
            x: Int,
            y: Int,
            value: Boolean
        ) {
            this[x][y] = value
        }

        /**
         * Get points in polygon (from flood fill). Result is cached.
         */
        fun pointsInPolygon(): Collection<PointKt> =
            filledPoints ?: compute().also { filledPoints = it }

        /**
         * Computation for [pointsInPolygon]
         */
        private fun compute(): Collection<PointKt> {
            // Normalized point path
            val offsetPath =
                path.map {
                    PointKt(
                        it.x - boundingBox.minX,
                        it.y - boundingBox.minY
                    )
                }

            // Normalized flag grid
            val flagged: Array<BooleanArray> =
                Array(boundingBox.sizeX) { BooleanArray(boundingBox.sizeY) }
            // Mark edges as visited to create boundary
            offsetPath.forEach {
                flagged[it.x, it.y] = true
            }
            // Context for computation. GC'd after use
            val context =
                Context(flagged = flagged, filledPoints = mutableSetOf())
            /*
             * Given the requirements from our path and bounding box, we know that the left most edge
             * exists along x = 0, and that the path is never adjacent to ourselves. With that,
             * we know that a point is inside the polygon if it is immediately to the right of a path point at x = 0.
             * This also translates to the first point at x = 1 that is below a path point, which is easier to compute.
             */
            val initialY =
                offsetPath.asSequence().filter { it.x == 1 }.map { it.y }
                    .minOrNull()?.let { it + 1 }
                    ?: throw IllegalStateException("Invalid path or bounding box.")

            context.subFill(1, initialY)
            return context.filledPoints.sorted()
        }

        /**
         * Variant of [subFill] that crawls to the top left non flagged point beforehand.
         * As a slight optimization, moving up is preferred to moving left, so we try to move upwards whenever possible.
         *
         * In the example below, starting at (4, 2) will result in a subfill starting at (3, 1) rather than (1, 2)
         * -------------
         * . . . . .
         * . . . o .
         * . o o o o
         */
        private fun Context.subFillFromTopLeft(testX: Int, testY: Int) {
            var x = testX
            var y = testY
            while (true) {
                val currX = x
                val currY = y
                while (y != 0 && !flagged[x, y - 1]) y--
                if (x != 0 && !flagged[x - 1, y]) x--
                if (x == currX && y == currY) break
            }
            subFill(x, y)
        }

        /**
         * Main part of flood fill.
         * The basis of span filling is to fill points along a row, and to move up and down to fill other rows.
         * In our algorithm, we start on the top left, and move our way down and to the right.
         * Each subFill is responsible for filling rows up until a boundary is reached,
         * and for filling subsequent rows downwards when the start point is reachable from the previous start point.
         * Recursion occurs for cases where a boundary splits a row in two, such as a `V` shaped polygon.
         *
         * Algorithm will start at [testX], [testY]. That point is expected not to be flagged.
         */
        private fun Context.subFill(testX: Int, testY: Int) {
            // Provided point is not flagged; fast return
            if (flagged[testX, testY]) return
            var x = testX
            var y = testY
            var prevRowLength = 0
            do {
                // Init. Mark current row length as 0 and add start x marker.
                var rowLength = 0
                var startX = x
                /*
                 * Handles cases such as
                 *
                 * ---------------------
                 * . o o o
                 * . . o o
                 *
                 * where the start x is rightwards of the previous start x.
                 * To support this, we shorted the previous row length and increment our start x until we reach an unflagged value,
                 * or until the prev row length is empty.
                 *
                 * Unlike the reference algorithm, we do not check that the last row length != 0.
                 * This is because we have a lot of unnecessary loops through recursion, and we wish to fast stop as soon as possible
                 */
                if (flagged[x, y]) {
                    ((x + 1) until (x + prevRowLength))
                        .asSequence()
//                        .onEach { prevRowLength-- }
                        .takeWhile { flagged[it, y] }
                        .count().let {
                            prevRowLength -= it + 1
                            x += it + 1
                            startX = x
                        }
//                    do {
//                        if (--prevRowLength <= 0) return
//                    } while (flagged[++x, y])
//                    startX = x
                }
                /*
                 * Handles cases such as
                 *
                 * ---------------------
                 * . . o o
                 * . o o o
                 *
                 * where the start x is leftwards of the previous start x.
                 * To support this, we increment both current and prev row length, and move x backwards;
                 * incrementing rows is necessary to broaden our row search for adjacent rows.
                 * Additionally, we will flag each point we go through and add them to filled points so as to avoid duplicate handling;
                 * as a result, we do not change start x.
                 * During our iteration, we check the row above for unmarked points,
                 * and recurse if necessary to handle previously unreachable rows (to the top left).
                 *
                 * Example, with `x` marking first starting point:
                 *
                 * ---------------------
                 * o o o . . x o o
                 * . o o o o o o .
                 */
                else {
                    ((x - 1) downTo 1)
                        .asSequence()
                        .takeWhile { !flagged[it, y] }
                        .onEach {
                            addFilledPoint(it, y)
                            flagged[it, y] = true
                            if (y != 0 && !flagged[it, y - 1])
                                subFillFromTopLeft(it, y - 1)
                        }
                        .count().let {
                            x -= it
                            rowLength += it
                            prevRowLength += it
                        }
//                    while (x != 0 && !flagged[x - 1, y]) {
//                        x--
//                        addFilledPoint(x, y)
//                        flagged[x, y] = true
//                        if (y != 0 && !flagged[x, y - 1])
//                            subFillFromTopLeft(x, y - 1)
//                        rowLength++
//                        prevRowLength++
//                    }
                }
                /*
                 * Scan through (remainder of) current row. Flag new points and add to filled points,
                 * Update start x and rowLength to match last point in row.
                 */
                (startX until boundingBox.sizeX)
                    .asSequence()
                    .takeWhile { !flagged[it, y] }
                    .onEach {
                        addFilledPoint(it, y)
                        flagged[it, y] = true
                    }
                    .count().let {
                        startX += it
                        rowLength += it
                    }

                /*
                 * Handles end cases such as
                 *
                 * ---------------------
                 * o o o o o
                 * o o o . o
                 *
                 * where a boundary exists in the current row.
                 * In this case, our row length is less than before,
                 * and we can iterate through the remaining points in the prev row length and check for unflagged points (potential new rows).
                 */
                if (rowLength < prevRowLength) {
                    ((startX + 1) until (x + prevRowLength))
                        .asSequence()
                        .filterNot { flagged[it, y] }
                        .forEach { subFill(it, y) }
                }
                /*
                 * Handles end cases such as
                 *
                 * ---------------------
                 * o o o . o
                 * o o o o o
                 *
                 * where a boundary exists in the previous row.
                 * In this case, our row length is larger than before,
                 * and we can iterate through the remaining points after the prev row length and check for unflagged points (potential new rows).
                 */
                else if (rowLength > prevRowLength && y != 0) {
                    ((x + prevRowLength + 1) until startX)
                        .asSequence()
                        .filterNot { flagged[it, y - 1] }
                        .forEach { subFillFromTopLeft(it, y - 1) }
                }

                prevRowLength = rowLength
            } while (prevRowLength != 0 && ++y < boundingBox.sizeY)
        }
    }

    /**
     * Returns a collection of points that are within the polygon.
     * Points include values in [path], and are sorted (by y, then x)
     */
    fun pointsInPolygon(): Collection<PointKt> = floodFill.pointsInPolygon()

}