package ca.allanwang.minecraft.toolbox.core

import kotlin.math.max
import kotlin.math.min

/**
 * Consumes points and keeps track of min/max values for each dimension.
 */
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