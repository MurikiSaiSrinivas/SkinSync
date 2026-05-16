package com.oo.skinsync.color

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Color-theory harmonies computed in OkLab (perceptually even hue rotation).
 * Used by wardrobe mode to suggest outfit colors that match a garment.
 */
object Harmony {

    data class Palette(
        val base: Int,
        val complement: Int,
        val analogous: List<Int>,
        val triadic: List<Int>,
    )

    private fun rotate(argb: Int, degrees: Double): Int {
        val lab = OkLab.toLab(argb)
        val chroma = hypot(lab.a, lab.b)
        val hue = atan2(lab.b, lab.a) + Math.toRadians(degrees)
        return OkLab.toArgb(OkLab.Lab(lab.l, chroma * cos(hue), chroma * sin(hue)))
    }

    fun of(base: Int): Palette = Palette(
        base = base or (0xFF shl 24),
        complement = rotate(base, 180.0),
        analogous = listOf(rotate(base, -30.0), rotate(base, 30.0)),
        triadic = listOf(rotate(base, 120.0), rotate(base, 240.0)),
    )

    /** Flat list (base first) for simple UI rendering. */
    fun list(base: Int): List<Int> = with(of(base)) {
        listOf(this.base) + analogous + listOf(complement) + triadic
    }
}
