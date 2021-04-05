package ca.allanwang.minecraft.toolbox.core

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.fail

class PolygonDataTest {

    private data class PolygonTestData(
        val data: PolygonData,
        val inside: List<PointKt>
    ) {

        val path get() = data.path
        val sizeX get() = data.boundingBox.sizeX
        val sizeY get() = data.boundingBox.sizeY

        private val prettyString: String by lazy {
            val pathGraph = data.path.groupBySet({ it.x }, { it.y })
            val insideGraph = inside.groupBySet({ it.x }, { it.y })

            buildString {
                (0 until sizeY).forEach { y ->
                    (0 until sizeX).forEach { x ->
                        val c = when {
                            pathGraph[x]?.contains(y) == true -> 'x'
                            insideGraph[x]?.contains(y) == true -> '-'
                            else -> '.'
                        }
                        append(c)
                        if (x < sizeX - 1) append(' ')
                    }
                    if (y < sizeY - 1) appendLine()
                }
            }
        }

        fun prettyString() = prettyString
    }

    private fun String.parsePolygonTestData(): PolygonTestData {
        val lines = lines()

        val sizeX = lines.map { it.replace(" ", "").length }.toSet()
            .takeIf { it.size == 1 }?.firstOrNull()
            ?: fail("Path has mismatched sizeX")

        val sizeY = lines.count()

        require(sizeX < 50) { "SizeX too big" }
        require(sizeY < 50) { "SizeY too big" }

        val boundingBox = PolygonData.BoundingBox(0, 0, sizeX, sizeY)

        val path = mutableListOf<PointKt>()
        val inside = mutableListOf<PointKt>()

        lines.forEachIndexed { y, line ->
            line.replace(" ", "").forEachIndexed { x, c ->
                when (c) {
                    'x' -> path.add(PointKt(x, y))
                    '-' -> inside.add(PointKt(x, y))
                    '.' -> Unit // ignore
                    else -> fail("Invalid char $c in path gen")
                }
            }
        }

        val data = PolygonData(
            path = path,
            boundingBox = boundingBox
        )

        return PolygonTestData(
            data = data,
            inside = inside
        )
    }

    private fun String.checkParsePolygon() {
        val polygon = parsePolygonTestData()
        val pretty = polygon.prettyString()

        assertThat(polygon).isEqualTo(pretty.parsePolygonTestData())
    }

    private fun points(vararg points: Pair<Int, Int>): List<PointKt> =
        points.map { (x, y) -> PointKt(x, y) }

    @Test
    fun basicParse() {
        val path = """
            x x x .
            x . x .
            x . x .
        """.trimIndent()

        val data = path.parsePolygonTestData()

        assertThat(data.sizeX).isEqualTo(4)
        assertThat(data.sizeY).isEqualTo(3)
        assertThat(data.inside).isEmpty()
        assertThat(data.path).isEqualTo(
            points(
                0 to 0,
                1 to 0,
                2 to 0,
                0 to 1,
                2 to 1,
                0 to 2,
                2 to 2
            )
        )
    }

    val path1 = """
        x x x . . . x x x
        x - x x . . x - x
        x - - x x x x - x
        x x - - - - - - x
        . x x x - - - x x
        . . . x x x x x .
    """.trimIndent()

    @Test
    fun complexParse() {
        path1.checkParsePolygon()
    }

    @Test
    fun complexExtract1() {
        val testData =
            path1.parsePolygonTestData()

        assertThat(testData.data.pointsInPolygon()).isEqualTo(
            points(
                1 to 1,
                7 to 1,
                1 to 2,
                2 to 2,
                7 to 2,
                2 to 3,
                3 to 3,
                4 to 3,
                5 to 3,
                6 to 3,
                7 to 3,
                4 to 4,
                5 to 4,
                6 to 4,
            )
        )
    }

}