package com.oo.skinsync.ml

/**
 * MediaPipe FaceLandmarker (478-point mesh) indices for the regions we sample.
 * Ported from the original SkinSync.
 */
object FacialLandmarkConstants {
    val CHEEK = listOf(234, 454, 361, 132)
    val LIP = listOf(61, 91, 181, 84, 17, 314, 405, 321, 375, 409, 270, 269, 267, 0, 37, 39, 40)
    val LEFT_EYE = listOf(469, 470, 471, 472)
    val RIGHT_EYE = listOf(374, 474, 475, 476)
}
