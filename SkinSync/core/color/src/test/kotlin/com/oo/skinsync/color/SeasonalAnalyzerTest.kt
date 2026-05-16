package com.oo.skinsync.color

import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonalAnalyzerTest {

    @Test
    fun `warm light skin classifies as a Spring`() {
        val r = SeasonalAnalyzer.analyze(
            skin = 0xFFF1C9A5.toInt(), lip = 0xFFD98A6A.toInt(), eye = 0xFF8A5A33.toInt(),
        )
        assertTrue(r, r.endsWith("Spring"))
    }

    @Test
    fun `cool deep features classify as a Winter`() {
        val r = SeasonalAnalyzer.analyze(
            skin = 0xFF6B4A52.toInt(), lip = 0xFF8E2B45.toInt(), eye = 0xFF20242E.toInt(),
        )
        assertTrue(r, r.endsWith("Winter"))
    }

    @Test
    fun `warm deep skin classifies as an Autumn`() {
        val r = SeasonalAnalyzer.analyze(
            skin = 0xFF7A5331.toInt(), lip = 0xFF9C5234.toInt(), eye = 0xFF4A3318.toInt(),
        )
        assertTrue(r, r.endsWith("Autumn"))
    }

    @Test
    fun `result always has a modifier and a season word`() {
        val r = SeasonalAnalyzer.analyze(0xFFCCB39A.toInt(), 0xFFB06A66.toInt(), 0xFF5C4633.toInt())
        assertTrue(r, r.trim().split(" ").size == 2)
    }

    @Test
    fun `deterministic`() {
        val a = SeasonalAnalyzer.analyze(0xFFE3B59B.toInt(), 0xFFB23A4F.toInt(), 0xFF5A4632.toInt())
        val b = SeasonalAnalyzer.analyze(0xFFE3B59B.toInt(), 0xFFB23A4F.toInt(), 0xFF5A4632.toInt())
        assertTrue(a == b)
    }
}
