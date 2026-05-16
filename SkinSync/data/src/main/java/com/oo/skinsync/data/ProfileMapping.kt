package com.oo.skinsync.data

import com.oo.skinsync.domain.FaceColors
import com.oo.skinsync.domain.Gender
import com.oo.skinsync.domain.Profile

/**
 * Pure (android-free) translation between [Profile] and a flat string map,
 * so the DataStore mapping is JVM-unit-testable.
 */
object ProfileMapping {
    const val NAME = "name"
    const val AGE = "age"
    const val GENDER = "gender"
    const val SKIN = "skin"
    const val LIP = "lip"
    const val LEFT_EYE = "left_eye"
    const val RIGHT_EYE = "right_eye"

    fun toMap(p: Profile): Map<String, String> = buildMap {
        put(NAME, p.name)
        p.age?.let { put(AGE, it.toString()) }
        put(GENDER, p.gender.name)
        p.faceColors?.let {
            put(SKIN, it.skin.toString())
            put(LIP, it.lip.toString())
            put(LEFT_EYE, it.leftEye.toString())
            put(RIGHT_EYE, it.rightEye.toString())
        }
    }

    fun fromMap(m: Map<String, String?>): Profile {
        val skin = m[SKIN]?.toIntOrNull()
        val lip = m[LIP]?.toIntOrNull()
        val le = m[LEFT_EYE]?.toIntOrNull()
        val re = m[RIGHT_EYE]?.toIntOrNull()
        val colors = if (skin != null && lip != null && le != null && re != null)
            FaceColors(skin, lip, le, re) else null
        return Profile(
            name = m[NAME].orEmpty(),
            age = m[AGE]?.toIntOrNull(),
            gender = m[GENDER]?.let { runCatching { Gender.valueOf(it) }.getOrNull() } ?: Gender.FEMALE,
            faceColors = colors,
        )
    }
}
