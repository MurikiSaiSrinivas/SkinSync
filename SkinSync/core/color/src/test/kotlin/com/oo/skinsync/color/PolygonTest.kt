package com.oo.skinsync.color

import com.oo.skinsync.color.Polygon.Point
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PolygonTest {

    private val square = listOf(Point(0f, 0f), Point(10f, 0f), Point(10f, 10f), Point(0f, 10f))

    @Test
    fun `point inside square`() = assertTrue(Polygon.contains(square, 5f, 5f))

    @Test
    fun `point outside square`() = assertFalse(Polygon.contains(square, 15f, 5f))

    @Test
    fun `degenerate polygon contains nothing`() {
        assertFalse(Polygon.contains(listOf(Point(0f, 0f), Point(1f, 1f)), 0.5f, 0.5f))
    }

    @Test
    fun `collects only pixels inside the polygon`() {
        val w = 4
        val h = 4
        // distinct value per pixel = index
        val pixels = IntArray(w * h) { 0xFF000000.toInt() or it }
        // polygon covering the central 2x2 block (x in 1..2, y in 1..2)
        val poly = listOf(Point(1f, 1f), Point(3f, 1f), Point(3f, 3f), Point(1f, 3f))
        val inside = Polygon.pixelsInside(pixels, w, h, poly).toSet()
        // centers (1.5,1.5)(2.5,1.5)(1.5,2.5)(2.5,2.5) -> indices 5,6,9,10
        assertEquals(setOf(0xFF000000.toInt() or 5, 0xFF000000.toInt() or 6, 0xFF000000.toInt() or 9, 0xFF000000.toInt() or 10), inside)
    }
}
