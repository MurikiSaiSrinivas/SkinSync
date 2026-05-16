package com.oo.skinsync.data

import com.oo.skinsync.domain.ColorRec
import com.oo.skinsync.domain.SavedLook
import com.oo.skinsync.domain.Suggestion
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Pure (android-free) mapping between [SavedLook] and a flat Firestore-style
 * field map. The palette is stored as a JSON string to keep the document
 * shape simple and the mapping fully unit-testable.
 */
object LookMapping {

    const val CREATED_AT = "createdAt"
    const val LOCATION = "location"
    const val SEASONAL_TYPE = "seasonalType"
    const val PALETTE_JSON = "paletteJson"
    const val FAVORITE = "favorite"

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class RecJson(
        val hexColor: String,
        val name: String,
        val reason: String,
        val outfitQueries: List<String>,
    )

    fun toMap(location: String, suggestion: Suggestion, createdAt: Long): Map<String, Any> {
        val recs = suggestion.palette.map { RecJson(it.hexColor, it.name, it.reason, it.outfitQueries) }
        return mapOf(
            CREATED_AT to createdAt,
            LOCATION to location,
            SEASONAL_TYPE to suggestion.seasonalType,
            PALETTE_JSON to json.encodeToString(recs),
            FAVORITE to false,
        )
    }

    fun fromDoc(id: String, fields: Map<String, Any?>): SavedLook {
        val recs = (fields[PALETTE_JSON] as? String)
            ?.let { runCatching { json.decodeFromString<List<RecJson>>(it) }.getOrDefault(emptyList()) }
            ?: emptyList()
        return SavedLook(
            id = id,
            createdAt = (fields[CREATED_AT] as? Number)?.toLong() ?: 0L,
            location = fields[LOCATION] as? String ?: "",
            suggestion = Suggestion(
                seasonalType = fields[SEASONAL_TYPE] as? String ?: "",
                palette = recs.map { ColorRec(it.hexColor, it.name, it.reason, it.outfitQueries) },
            ),
            favorite = fields[FAVORITE] as? Boolean ?: false,
        )
    }
}
