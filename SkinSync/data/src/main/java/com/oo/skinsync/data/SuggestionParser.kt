package com.oo.skinsync.data

import com.oo.skinsync.domain.ColorRec
import com.oo.skinsync.domain.Suggestion
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** DTOs matching the structured JSON the model is asked to return. */
@Serializable
private data class SuggestionDto(
    val seasonalType: String = "",
    val palette: List<ColorRecDto> = emptyList(),
)

@Serializable
private data class ColorRecDto(
    val hexColor: String = "",
    val name: String = "",
    val reason: String = "",
    val outfitQueries: List<String> = emptyList(),
)

/**
 * Pure JSON → domain mapping. Replaces the old brittle string-replace hacks
 * (`.replace("```")`). Tolerant of markdown fences and unknown keys.
 */
object SuggestionParser {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    fun parse(raw: String): Result<Suggestion> = runCatching {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```")
            .trim()
        val dto = json.decodeFromString<SuggestionDto>(cleaned)
        val palette = dto.palette
            .filter { it.hexColor.isNotBlank() }
            .map {
                ColorRec(
                    hexColor = normalizeHex(it.hexColor),
                    name = it.name.ifBlank { "Color" },
                    reason = it.reason,
                    outfitQueries = it.outfitQueries.filter(String::isNotBlank),
                )
            }
        require(palette.isNotEmpty()) { "AI returned no colors." }
        Suggestion(seasonalType = dto.seasonalType, palette = palette)
    }

    private fun normalizeHex(h: String): String {
        val s = h.trim().removePrefix("#").take(6).padEnd(6, '0')
        return "#${s.uppercase()}"
    }
}
