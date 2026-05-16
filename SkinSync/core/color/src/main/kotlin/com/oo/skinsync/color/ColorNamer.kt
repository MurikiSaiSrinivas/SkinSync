package com.oo.skinsync.color

import kotlin.math.atan2
import kotlin.math.hypot

/** Coarse human color name from an ARGB int (for shopping search phrases). */
object ColorNamer {

    fun name(argb: Int): String {
        val lab = OkLab.toLab(argb)
        val chroma = hypot(lab.a, lab.b)
        if (chroma < 0.04) return when {
            lab.l > 0.8 -> "white"
            lab.l < 0.2 -> "black"
            else -> "grey"
        }
        val hue = (Math.toDegrees(atan2(lab.b, lab.a)) + 360.0) % 360.0
        return when {
            hue < 20 || hue >= 345 -> "red"
            hue < 50 -> "orange"
            hue < 70 -> "yellow"
            hue < 160 -> "green"
            hue < 200 -> "teal"
            hue < 260 -> "blue"
            hue < 300 -> "purple"
            else -> "pink"
        }
    }
}
