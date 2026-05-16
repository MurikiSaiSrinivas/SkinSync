package com.oo.skinsync.color

import kotlin.math.sqrt

/**
 * Offline, deterministic seasonal color classification (the classic
 * Spring / Summer / Autumn / Winter system) derived from OkLab face values.
 * Pure Kotlin — complements the AI's seasonalType and works with no network.
 */
object SeasonalAnalyzer {

    /**
     * @param skin/lip/eye opaque ARGB ints sampled from the face.
     * @return e.g. "Warm Autumn", "Cool Winter", "Light Spring", "Soft Summer".
     */
    fun analyze(skin: Int, lip: Int, eye: Int): String {
        val s = OkLab.toLab(skin)
        val l = OkLab.toLab(lip)

        // Warm vs cool: OkLab b (yellow+/blue-) plus lip influence.
        val warmth = s.b * 0.7 + l.b * 0.3
        val isWarm = warmth >= 0.0

        // Depth from skin lightness.
        val deep = s.l < 0.62

        // Chroma (clarity) from lip saturation — bright vs muted.
        val chroma = sqrt(l.a * l.a + l.b * l.b)
        val bright = chroma >= 0.12

        val season = when {
            isWarm && !deep -> "Spring"
            isWarm && deep -> "Autumn"
            !isWarm && !deep -> "Summer"
            else -> "Winter"
        }
        val modifier = when (season) {
            "Spring" -> if (bright) "Bright" else "Light"
            "Autumn" -> if (bright) "Warm" else "Soft"
            "Summer" -> if (bright) "Cool" else "Soft"
            else -> if (bright) "Bright" else "Cool" // Winter
        }
        return "$modifier $season"
    }
}
