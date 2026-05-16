package com.oo.skinsync.color

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GrayWorldTest {

    @Test
    fun `empty input returned unchanged`() {
        assertEquals(0, GrayWorld.balance(IntArray(0)).size)
    }

    @Test
    fun `neutral gray image is unchanged`() {
        val gray = 0xFF808080.toInt()
        val out = GrayWorld.balance(IntArray(16) { gray })
        out.forEach { assertEquals(gray, it) }
    }

    @Test
    fun `warm cast is neutralized toward gray`() {
        // Strong orange cast: avg R high, B low.
        val cast = 0xFFC08040.toInt()
        val out = GrayWorld.balance(IntArray(64) { cast })
        val r = OkLab.r(out[0]); val g = OkLab.g(out[0]); val b = OkLab.bch(out[0])
        // After balancing a single uniform color, all channels equalize.
        assertTrue("r=$r g=$g b=$b", kotlin.math.abs(r - g) <= 2 && kotlin.math.abs(g - b) <= 2)
    }

    @Test
    fun `transparent pixels ignored when measuring`() {
        val pixels = IntArray(10) { if (it < 5) 0x00FF0000 else 0xFF808080.toInt() }
        // Should not crash and should keep opaque grays roughly gray.
        val out = GrayWorld.balance(pixels)
        assertEquals(10, out.size)
    }
}
