package com.oo.skinsync.feature.wardrobe

/** Wardrobe flow state. Every case is rendered explicitly (rule #6). */
sealed interface WardrobeState {
    data object NeedsConsent : WardrobeState
    data object Ready : WardrobeState
    data object Analyzing : WardrobeState
    data class Result(
        val baseColor: Int,
        val matches: List<Int>,
        val colorName: String,
    ) : WardrobeState
    data class Error(val message: String) : WardrobeState
}
