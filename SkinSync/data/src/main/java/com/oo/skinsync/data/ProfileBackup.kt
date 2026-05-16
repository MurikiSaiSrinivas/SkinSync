package com.oo.skinsync.data

import com.oo.skinsync.domain.FaceColors
import com.oo.skinsync.domain.Gender
import com.oo.skinsync.domain.Profile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Pure JSON backup/restore of a [Profile] (no Android). Lets the user export
 * and re-import their profile + extracted colors.
 */
object ProfileBackup {

    @Serializable
    private data class ColorsDto(val skin: Int, val lip: Int, val leftEye: Int, val rightEye: Int)

    @Serializable
    private data class ProfileDto(
        val name: String = "",
        val age: Int? = null,
        val gender: String = Gender.FEMALE.name,
        val colors: ColorsDto? = null,
        val version: Int = 1,
    )

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    fun export(profile: Profile): String = json.encodeToString(
        ProfileDto(
            name = profile.name,
            age = profile.age,
            gender = profile.gender.name,
            colors = profile.faceColors?.let { ColorsDto(it.skin, it.lip, it.leftEye, it.rightEye) },
        ),
    )

    fun import(text: String): Result<Profile> = runCatching {
        val dto = json.decodeFromString<ProfileDto>(text.trim())
        Profile(
            name = dto.name,
            age = dto.age,
            gender = runCatching { Gender.valueOf(dto.gender) }.getOrDefault(Gender.FEMALE),
            faceColors = dto.colors?.let { FaceColors(it.skin, it.lip, it.leftEye, it.rightEye) },
        )
    }
}
