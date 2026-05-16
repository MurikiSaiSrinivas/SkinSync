package com.oo.skinsync.color

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DominantColorTest {

    @Test
    fun `empty input returns null`() {
        assertNull(DominantColor.of(IntArray(0)))
    }

    @Test
    fun `all transparent returns null`() {
        assertNull(DominantColor.of(IntArray(50) { 0x00FFFFFF }))
    }

    @Test
    fun `dominant of a skin-heavy sample is skin-ish, not the few outliers`() {
        val skin = 0xFFE3B59B.toInt()
        val pixels = IntArray(300) { if (it < 270) skin else 0xFF0000FF.toInt() }
        val result = DominantColor.of(pixels)!!
        // Closer to skin than to the blue outliers.
        val dSkin = OkLab.distance2(OkLab.toLab(result), OkLab.toLab(skin))
        val dBlue = OkLab.distance2(OkLab.toLab(result), OkLab.toLab(0xFF0000FF.toInt()))
        assertTrue(dSkin < dBlue)
    }

    @Test
    fun `deterministic for a fixed seed`() {
        val pixels = IntArray(200) { 0xFFAA3388.toInt() or (it and 0x0F) }
        assertEquals(DominantColor.of(pixels, seed = 7), DominantColor.of(pixels, seed = 7))
    }
}
