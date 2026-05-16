package com.oo.skinsync.ml

import com.oo.skinsync.ml.FaceColorMapper.NLandmark
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceColorMapperTest {

    /** Build a landmark list where the four regions sit in four image quadrants. */
    private fun landmarks(): List<NLandmark> {
        val l = MutableList(478) { NLandmark(0.5f, 0.5f) }
        // cheek = 234,454,361,132 -> top-left quadrant box
        listOf(234, 454, 361, 132).zip(
            listOf(NLandmark(0.05f, 0.05f), NLandmark(0.45f, 0.05f), NLandmark(0.45f, 0.45f), NLandmark(0.05f, 0.45f))
        ).forEach { (i, p) -> l[i] = p }
        // lip indices -> top-right box
        FacialLandmarkConstants.LIP.forEachIndexed { k, i ->
            l[i] = listOf(NLandmark(0.55f, 0.05f), NLandmark(0.95f, 0.05f), NLandmark(0.95f, 0.45f), NLandmark(0.55f, 0.45f))[k % 4]
        }
        // left eye -> bottom-left box
        listOf(469, 470, 471, 472).zip(
            listOf(NLandmark(0.05f, 0.55f), NLandmark(0.45f, 0.55f), NLandmark(0.45f, 0.95f), NLandmark(0.05f, 0.95f))
        ).forEach { (i, p) -> l[i] = p }
        // right eye -> bottom-right box
        listOf(374, 474, 475, 476).zip(
            listOf(NLandmark(0.55f, 0.55f), NLandmark(0.95f, 0.55f), NLandmark(0.95f, 0.95f), NLandmark(0.55f, 0.95f))
        ).forEach { (i, p) -> l[i] = p }
        return l
    }

    @Test
    fun `maps each quadrant to its dominant color`() {
        val w = 100; val h = 100
        val skin = 0xFFE3B59B.toInt(); val lip = 0xFFB23A4F.toInt()
        val eye = 0xFF5A4632.toInt()
        val pixels = IntArray(w * h) { idx ->
            val x = idx % w; val y = idx / w
            when {
                x < 50 && y < 50 -> skin
                x >= 50 && y < 50 -> lip
                else -> eye
            }
        }
        val fc = FaceColorMapper.colorsFrom(pixels, w, h, landmarks())
        assertNotNull(fc)
        // dominant of a uniform region equals that region's color (allow tiny rounding)
        assertTrue(close(fc!!.skin, skin)); assertTrue(close(fc.lip, lip))
        assertTrue(close(fc.leftEye, eye)); assertTrue(close(fc.rightEye, eye))
    }

    @Test
    fun `returns null when landmarks missing`() {
        assertNull(FaceColorMapper.colorsFrom(IntArray(100), 10, 10, emptyList()))
    }

    private fun close(a: Int, b: Int): Boolean {
        fun ch(v: Int, s: Int) = (v ushr s) and 0xFF
        return (16 downTo 0 step 8).all { kotlin.math.abs(ch(a, it) - ch(b, it)) <= 3 }
    }
}
