package ca.allanwang.minecraft.toolbox.core

data class PointKt(val x: Int, val y: Int) : Comparable<PointKt> {
    override fun toString(): String = "($x, $y)"

    override fun compareTo(other: PointKt): Int =
        y.compareTo(other.y).takeIf { it != 0 } ?: x.compareTo(other.x)
}