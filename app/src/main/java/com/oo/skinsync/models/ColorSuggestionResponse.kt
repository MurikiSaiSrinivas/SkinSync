package com.oo.skinsync.models

data class ColorSuggestionResponse(
    val suggestedColors: List<SuggestedColor>
)
data class SuggestedColor(
    val color: Int,
    val reason: String
)