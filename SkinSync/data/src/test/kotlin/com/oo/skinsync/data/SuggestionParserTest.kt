package com.oo.skinsync.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionParserTest {

    @Test
    fun `parses clean json`() {
        val json = """
            {"seasonalType":"Soft Autumn","palette":[
              {"hexColor":"#A23BA0","name":"Orchid","reason":"warm","outfitQueries":["orchid dress"]}
            ]}
        """.trimIndent()
        val s = SuggestionParser.parse(json).getOrThrow()
        assertEquals("Soft Autumn", s.seasonalType)
        assertEquals("#A23BA0", s.palette.first().hexColor)
        assertEquals("Orchid", s.palette.first().name)
    }

    @Test
    fun `strips markdown fences`() {
        val json = "```json\n{\"seasonalType\":\"X\",\"palette\":[{\"hexColor\":\"abc\",\"name\":\"\",\"reason\":\"r\",\"outfitQueries\":[]}]}\n```"
        val s = SuggestionParser.parse(json).getOrThrow()
        assertEquals("#ABC000", s.palette.first().hexColor) // normalized + padded
        assertEquals("Color", s.palette.first().name) // blank name defaulted
    }

    @Test
    fun `ignores unknown keys`() {
        val json = """{"seasonalType":"Y","extra":1,"palette":[{"hexColor":"#fff","name":"W","reason":"","outfitQueries":["a"],"junk":true}]}"""
        assertTrue(SuggestionParser.parse(json).isSuccess)
    }

    @Test
    fun `empty palette fails`() {
        assertTrue(SuggestionParser.parse("""{"seasonalType":"Z","palette":[]}""").isFailure)
    }

    @Test
    fun `garbage fails gracefully`() {
        assertTrue(SuggestionParser.parse("not json at all").isFailure)
    }
}
