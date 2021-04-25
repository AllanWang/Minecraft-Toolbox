package ca.allanwang.minecraft.toolbox.core

import kotlin.math.max
import kotlin.math.min

/**
 * Consumes points and keeps track of min/max values for each dimension (2D).
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

    inline val sizeX: Int get() = maxX - minX + 1
    inline val sizeY: Int get() = maxY - minY + 1

    val maxSize: Int get() = max(sizeX, sizeY)
}

/**
 * Consumes points and keeps track of min/max values for each dimension (3D).
 */
data class BoundingPrism(
    var minX: Int,
    var minY: Int,
    var minZ: Int,
    var maxX: Int = minX,
    var maxY: Int = minY,
    var maxZ: Int = minZ,
) {
    fun bind(x: Int, y: Int, z: Int) {
        minX = min(minX, x)
        maxX = max(maxX, x)
        minY = min(minY, y)
        maxY = max(maxY, y)
        minZ = min(minZ, z)
        maxZ = max(maxZ, z)
    }

    inline val sizeX: Int get() = maxX - minX + 1
    inline val sizeY: Int get() = maxY - minY + 1
    inline val sizeZ: Int get() = maxZ - minZ + 1

    val maxSize: Int get() = max(sizeX, sizeY, sizeZ)
}