package com.oo.skinsync.models

data class ColorSuggestionResponse(
    val suggestedColors: List<SuggestedColor>
)

data class SuggestedColor(
    val color: String,
    val reason: String,
    val dresses: List<String>
)