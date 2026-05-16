package com.oo.skinsync.domain

/** Gender used to tailor outfit suggestions. */
enum class Gender { FEMALE, MALE, OTHER }

/**
 * Colors sampled from the user's face, as 0xAARRGGBB ints.
 * Platform-neutral: the camera/ML layer fills these in.
 */
data class FaceColors(
    val skin: Int,
    val lip: Int,
    val leftEye: Int,
    val rightEye: Int,
)

/** The persisted user profile. */
data class Profile(
    val name: String = "",
    val age: Int? = null,
    val gender: Gender = Gender.FEMALE,
    val faceColors: FaceColors? = null,
)

/** One recommended color plus why it works and what to search for. */
data class ColorRec(
    val hexColor: String,
    val name: String,
    val reason: String,
    val outfitQueries: List<String>,
)

/** Full AI recommendation for a location + profile. */
data class Suggestion(
    val seasonalType: String,
    val palette: List<ColorRec>,
)

/** A buyable search link the UI opens externally. */
data class ShoppingLink(
    val retailer: String,
    val query: String,
    val url: String,
)

/** A recommendation the user chose to keep. */
data class SavedLook(
    val id: String,
    val createdAt: Long,
    val location: String,
    val suggestion: Suggestion,
    val favorite: Boolean = false,
)
