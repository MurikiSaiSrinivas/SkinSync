package com.oo.skinsync.feature.result

import com.oo.skinsync.domain.ColorRec
import com.oo.skinsync.domain.Suggestion
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareTextBuilderTest {

    private val s = Suggestion(
        "Soft Autumn",
        listOf(
            ColorRec("#A23BA0", "Orchid", "warm", listOf("orchid dress")),
            ColorRec("#82524A", "Terracotta", "earthy", listOf("terracotta top")),
        ),
    )

    @Test
    fun `includes location, season and color names`() {
        val t = ShareTextBuilder.build("Goa beach", s)
        assertTrue(t.contains("Goa beach"))
        assertTrue(t.contains("Soft Autumn"))
        assertTrue(t.contains("Orchid"))
        assertTrue(t.contains("Terracotta"))
    }

    @Test
    fun `blank location falls back gracefully`() {
        val t = ShareTextBuilder.build("   ", s)
        assertTrue(t.contains("my shoot"))
    }
}
