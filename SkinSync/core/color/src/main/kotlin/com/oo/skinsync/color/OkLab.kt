package com.oo.skinsync.color

import kotlin.math.cbrt
import kotlin.math.pow

/**
 * sRGB <-> OkLab conversion (Björn Ottosson, 2020).
 * OkLab is perceptually uniform, so clustering skin tones here is far more
 * accurate than the old RGB K-means.
 *
 * Colors are platform-neutral ARGB ints (0xAARRGGBB).
 */
object OkLab {

    data class Lab(val l: Double, val a: Double, val b: Double)

    fun a(argb: Int) = (argb ushr 24) and 0xFF
    fun r(argb: Int) = (argb ushr 16) and 0xFF
    fun g(argb: Int) = (argb ushr 8) and 0xFF
    fun bch(argb: Int) = argb and 0xFF

    fun argb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or
            ((r.coerceIn(0, 255)) shl 16) or
            ((g.coerceIn(0, 255)) shl 8) or
            (b.coerceIn(0, 255))

    private fun srgbToLinear(c: Double): Double =
        if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

    private fun linearToSrgb(c: Double): Double =
        if (c <= 0.0031308) 12.92 * c else 1.055 * c.pow(1.0 / 2.4) - 0.055

    fun toLab(argb: Int): Lab {
        val lr = srgbToLinear(r(argb) / 255.0)
        val lg = srgbToLinear(g(argb) / 255.0)
        val lb = srgbToLinear(bch(argb) / 255.0)

        val l = 0.4122214708 * lr + 0.5363325363 * lg + 0.0514459929 * lb
        val m = 0.2119034982 * lr + 0.6806995451 * lg + 0.1073969566 * lb
        val s = 0.0883024619 * lr + 0.2817188376 * lg + 0.6299787005 * lb

        val l_ = cbrt(l)
        val m_ = cbrt(m)
        val s_ = cbrt(s)

        return Lab(
            l = 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_,
            a = 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_,
            b = 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_,
        )
    }

    fun toArgb(lab: Lab): Int {
        val l_ = lab.l + 0.3963377774 * lab.a + 0.2158037573 * lab.b
        val m_ = lab.l - 0.1055613458 * lab.a - 0.0638541728 * lab.b
        val s_ = lab.l - 0.0894841775 * lab.a - 1.2914855480 * lab.b

        val l = l_ * l_ * l_
        val m = m_ * m_ * m_
        val s = s_ * s_ * s_

        val lr = 4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s
        val lg = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s
        val lb = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s

        return argb(
            (linearToSrgb(lr) * 255.0).toInt(),
            (linearToSrgb(lg) * 255.0).toInt(),
            (linearToSrgb(lb) * 255.0).toInt(),
        )
    }

    /** Squared perceptual distance between two OkLab points. */
    fun distance2(p: Lab, q: Lab): Double {
        val dl = p.l - q.l
        val da = p.a - q.a
        val db = p.b - q.b
        return dl * dl + da * da + db * db
    }
}
