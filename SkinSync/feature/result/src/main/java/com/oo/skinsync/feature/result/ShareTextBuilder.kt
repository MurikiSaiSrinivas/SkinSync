package com.oo.skinsync.feature.result

import com.oo.skinsync.domain.Suggestion

/** Pure share caption builder (unit-testable, no Android). */
object ShareTextBuilder {
    fun build(location: String, suggestion: Suggestion): String {
        val names = suggestion.palette.joinToString(", ") { it.name }
        val where = location.trim().ifBlank { "my shoot" }
        return "My SkinSync palette for $where (${suggestion.seasonalType}): " +
            "$names. Found with the SkinSync app."
    }
}
