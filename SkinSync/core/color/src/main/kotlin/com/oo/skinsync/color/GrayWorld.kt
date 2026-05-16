package com.oo.skinsync.color

/**
 * Gray-world white balance. Assumes the average of a scene is neutral gray and
 * rescales each channel to remove a color cast (warm indoor light, blue shade).
 * This makes the extracted skin/lip/eye colors far more consistent across
 * lighting. Pure Kotlin, in-place-safe (returns a new array), unit-testable.
 */
object GrayWorld {

    /** @param pixels ARGB ints. Transparent pixels are ignored when measuring. */
    fun balance(pixels: IntArray): IntArray {
        if (pixels.isEmpty()) return pixels
        var sr = 0.0; var sg = 0.0; var sb = 0.0; var n = 0
        for (p in pixels) {
            if (OkLab.a(p) < 200) continue
            sr += OkLab.r(p); sg += OkLab.g(p); sb += OkLab.bch(p); n++
        }
        if (n == 0) return pixels
        val ar = sr / n; val ag = sg / n; val ab = sb / n
        val gray = (ar + ag + ab) / 3.0
        if (ar == 0.0 || ag == 0.0 || ab == 0.0) return pixels
        val kr = gray / ar; val kg = gray / ag; val kb = gray / ab

        return IntArray(pixels.size) { i ->
            val p = pixels[i]
            val a = (p ushr 24) and 0xFF
            val r = (OkLab.r(p) * kr).toInt().coerceIn(0, 255)
            val g = (OkLab.g(p) * kg).toInt().coerceIn(0, 255)
            val b = (OkLab.bch(p) * kb).toInt().coerceIn(0, 255)
            (a shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
}
