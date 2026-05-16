package com.oo.skinsync.color

/**
 * Pure-Kotlin polygon pixel sampling. The old app rasterized a full-size mask
 * Bitmap and called getPixel() per pixel — slow and Android-bound. This walks
 * only the polygon's bounding box and uses a ray-cast test, so it is fast and
 * unit-testable without Android.
 */
object Polygon {

    data class Point(val x: Float, val y: Float)

    /** Even-odd ray-casting point-in-polygon test. */
    fun contains(poly: List<Point>, x: Float, y: Float): Boolean {
        if (poly.size < 3) return false
        var inside = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val pi = poly[i]
            val pj = poly[j]
            if (((pi.y > y) != (pj.y > y)) &&
                (x < (pj.x - pi.x) * (y - pi.y) / (pj.y - pi.y) + pi.x)
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    /**
     * Collect ARGB pixels inside [poly]. [pixels] is row-major width*height.
     * Only the polygon bounding box is scanned.
     */
    fun pixelsInside(
        pixels: IntArray,
        width: Int,
        height: Int,
        poly: List<Point>,
    ): IntArray {
        if (poly.size < 3) return IntArray(0)
        val minX = poly.minOf { it.x }.toInt().coerceIn(0, width - 1)
        val maxX = poly.maxOf { it.x }.toInt().coerceIn(0, width - 1)
        val minY = poly.minOf { it.y }.toInt().coerceIn(0, height - 1)
        val maxY = poly.maxOf { it.y }.toInt().coerceIn(0, height - 1)

        val out = ArrayList<Int>((maxX - minX + 1) * (maxY - minY + 1) / 2 + 1)
        for (y in minY..maxY) {
            val row = y * width
            for (x in minX..maxX) {
                if (contains(poly, x + 0.5f, y + 0.5f)) out.add(pixels[row + x])
            }
        }
        return out.toIntArray()
    }
}
