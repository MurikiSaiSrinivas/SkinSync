package com.oo.skinsync.data

import com.oo.skinsync.domain.FaceColors
import com.oo.skinsync.domain.Gender
import com.oo.skinsync.domain.Profile
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    @Test
    fun `includes location, gender, age and hex colors`() {
        val p = Profile("A", 27, Gender.MALE, FaceColors(0xFFE3B59B.toInt(), 0xFFB23A4F.toInt(), 0xFF5A4632.toInt(), 0xFF5A4632.toInt()))
        val prompt = PromptBuilder.build(p, "Goa beach")

        assertTrue(prompt.contains("Goa beach"))
        assertTrue(prompt.contains("male"))
        assertTrue(prompt.contains("27"))
        assertTrue(prompt.contains("#E3B59B"))
        assertTrue(prompt.contains("\"seasonalType\""))
        assertTrue(prompt.contains("outfitQueries"))
    }

    @Test
    fun `handles missing colors and age`() {
        val prompt = PromptBuilder.build(Profile(name = "B"), "Paris")
        assertTrue(prompt.contains("Paris"))
        assertTrue(prompt.contains("unknown"))
    }
}
