package com.oo.skinsync.color

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OkLabTest {

    @Test
    fun `round trip is near-lossless for common colors`() {
        val samples = intArrayOf(
            0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFFFF0000.toInt(),
            0xFF00FF00.toInt(), 0xFF0000FF.toInt(), 0xFFE0B0A0.toInt(),
            0xFFB04050.toInt(), 0xFF808080.toInt(),
        )
        for (c in samples) {
            val back = OkLab.toArgb(OkLab.toLab(c))
            assertTrue("R off for ${Integer.toHexString(c)}", kotlin.math.abs(OkLab.r(c) - OkLab.r(back)) <= 2)
            assertTrue("G off for ${Integer.toHexString(c)}", kotlin.math.abs(OkLab.g(c) - OkLab.g(back)) <= 2)
            assertTrue("B off for ${Integer.toHexString(c)}", kotlin.math.abs(OkLab.bch(c) - OkLab.bch(back)) <= 2)
        }
    }

    @Test
    fun `distance is zero for identical colors`() {
        val lab = OkLab.toLab(0xFF123456.toInt())
        assertEquals(0.0, OkLab.distance2(lab, lab), 1e-9)
    }

    @Test
    fun `black is far from white`() {
        val d = OkLab.distance2(OkLab.toLab(0xFF000000.toInt()), OkLab.toLab(0xFFFFFFFF.toInt()))
        assertTrue(d > 0.5)
    }
}
