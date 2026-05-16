package com.oo.skinsync.ml

import com.oo.skinsync.color.DominantColor
import com.oo.skinsync.color.Polygon
import com.oo.skinsync.domain.FaceColors

/**
 * Pure (android-free) mapping from normalized landmarks + pixels to
 * [FaceColors]. Kept here, not in the MediaPipe impl, so it is JVM-unit-testable.
 */
object FaceColorMapper {

    /** Normalized landmark, x/y in [0,1]. */
    data class NLandmark(val x: Float, val y: Float)

    private fun region(
        pixels: IntArray, w: Int, h: Int,
        landmarks: List<NLandmark>, indices: List<Int>,
    ): Int? {
        val poly = indices.mapNotNull { i ->
            landmarks.getOrNull(i)?.let { Polygon.Point(it.x * w, it.y * h) }
        }
        if (poly.size < 3) return null
        val inside = Polygon.pixelsInside(pixels, w, h, poly)
        return DominantColor.of(inside)
    }

    /**
     * @return [FaceColors] or null if any region could not be sampled.
     */
    fun colorsFrom(
        pixels: IntArray, w: Int, h: Int, landmarks: List<NLandmark>,
    ): FaceColors? {
        val skin = region(pixels, w, h, landmarks, FacialLandmarkConstants.CHEEK) ?: return null
        val lip = region(pixels, w, h, landmarks, FacialLandmarkConstants.LIP) ?: return null
        val le = region(pixels, w, h, landmarks, FacialLandmarkConstants.LEFT_EYE) ?: return null
        val re = region(pixels, w, h, landmarks, FacialLandmarkConstants.RIGHT_EYE) ?: return null
        return FaceColors(skin = skin, lip = lip, leftEye = le, rightEye = re)
    }
}
