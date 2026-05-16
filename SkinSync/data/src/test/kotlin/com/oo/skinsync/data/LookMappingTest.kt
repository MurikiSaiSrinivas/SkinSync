package com.oo.skinsync.data

import com.oo.skinsync.domain.ColorRec
import com.oo.skinsync.domain.Suggestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LookMappingTest {

    private val suggestion = Suggestion(
        "Soft Autumn",
        listOf(ColorRec("#A23BA0", "Orchid", "warm", listOf("orchid dress", "orchid top"))),
    )

    @Test
    fun `round trips through a field map`() {
        val map = LookMapping.toMap("Goa beach", suggestion, 1234L)
        val look = LookMapping.fromDoc("doc1", map)

        assertEquals("doc1", look.id)
        assertEquals(1234L, look.createdAt)
        assertEquals("Goa beach", look.location)
        assertEquals("Soft Autumn", look.suggestion.seasonalType)
        assertEquals(suggestion.palette, look.suggestion.palette)
    }

    @Test
    fun `missing fields degrade gracefully`() {
        val look = LookMapping.fromDoc("d", emptyMap())
        assertEquals("", look.location)
        assertEquals(0L, look.createdAt)
        assertTrue(look.suggestion.palette.isEmpty())
        assertTrue(!look.favorite)
    }

    @Test
    fun `favorite flag round trips from doc`() {
        val look = LookMapping.fromDoc("d", mapOf(LookMapping.FAVORITE to true))
        assertTrue(look.favorite)
    }

    @Test
    fun `createdAt accepts numeric types`() {
        val look = LookMapping.fromDoc("d", mapOf(LookMapping.CREATED_AT to 99.0))
        assertEquals(99L, look.createdAt)
    }
}
