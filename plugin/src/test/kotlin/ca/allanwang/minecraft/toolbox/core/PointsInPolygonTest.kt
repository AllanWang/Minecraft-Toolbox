package ca.allanwang.minecraft.toolbox.core

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.awt.Point
import kotlin.test.fail

class PointsInPolygonTest {

    private fun String.parsePath(): List<Point> {
        assertThat(lineSequence().map { it.trim().length }.toSet()).hasSize(1)
        return lines().flatMapIndexed { y, line ->
            mutableListOf<Point>().apply {
                line.replace(" ", "").forEachIndexed { x, c ->
                    when (c) {
                        'x' -> add(Point(x, y))
                        '.' -> Unit // ignore
                        else -> fail("Invalid char $c in path gen")
                    }
                }
            }
        }
    }

    private fun List<Point>.prettyString(): String {
        val maxX = maxOfOrNull { it.x }?.takeIf { it < 20 }
            ?: fail("pretty print is empty or too large")
        val maxY = maxOfOrNull { it.y }?.takeIf { it < 20 }
            ?: fail("pretty print is empty or too large")

        val coordGraph = groupBySet({ it.x }, { it.y })

        return buildString {
            (0..maxY).forEach { y ->
                (0..maxX).forEach { x ->
                    val c = if (coordGraph.getValue(x).contains(y)) 'x' else '.'
                    append(c)
                    append(' ')
                }
                appendLine()
            }
        }.trim()
    }
    
    private fun String.checkPathParse() {
        val path = parsePath()
        val pretty = path.prettyString()
        
        assertThat(path).isEqualTo(pretty.parsePath())
    }

    @Test
    fun basicParse() {
        val path = """
            x x x
            x . x
            x . x
        """.trimIndent()

        assertThat(path.parsePath()).isEqualTo(
            listOf(
                Point(0, 0),
                Point(1, 0),
                Point(2, 0),
                Point(0, 1),
                Point(2, 1),
                Point(0, 2),
                Point(2, 2),
            )
        )
    }
    
    @Test
    fun complexParse() {
        val path = """
            x x x . . . x x x
            x . x x . . x . x
            x . . x x x x . x
            x x . . . . . . x
            . x x x . . . x x
            . . . x x x x x .
        """.trimIndent()
        
        path.checkPathParse()
    }

}