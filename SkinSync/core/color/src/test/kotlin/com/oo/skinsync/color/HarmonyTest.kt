package com.oo.skinsync.color

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HarmonyTest {

    @Test
    fun `palette has base, complement, analogous and triadic`() {
        val p = Harmony.of(0xFFA23BA0.toInt())
        assertEquals(0xFF, (p.base ushr 24) and 0xFF) // opaque
        assertEquals(2, p.analogous.size)
        assertEquals(2, p.triadic.size)
    }

    @Test
    fun `complement differs from base`() {
        val base = 0xFFA23BA0.toInt()
        assertTrue(Harmony.of(base).complement != base)
    }

    @Test
    fun `flat list starts with base and has six entries`() {
        val base = 0xFF3B82F6.toInt()
        val list = Harmony.list(base)
        assertEquals(6, list.size)
        assertEquals(base or (0xFF shl 24), list.first())
    }

    @Test
    fun `deterministic`() {
        assertEquals(Harmony.list(0xFF812B91.toInt()), Harmony.list(0xFF812B91.toInt()))
    }
}
