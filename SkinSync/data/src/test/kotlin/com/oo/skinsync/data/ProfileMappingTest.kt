package com.oo.skinsync.data

import com.oo.skinsync.domain.FaceColors
import com.oo.skinsync.domain.Gender
import com.oo.skinsync.domain.Profile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileMappingTest {

    @Test
    fun `round trips a full profile`() {
        val p = Profile("Sai", 24, Gender.MALE, FaceColors(1, 2, 3, 4))
        assertEquals(p, ProfileMapping.fromMap(ProfileMapping.toMap(p)))
    }

    @Test
    fun `profile without colors maps back with null faceColors`() {
        val p = Profile("A", null, Gender.OTHER, null)
        val back = ProfileMapping.fromMap(ProfileMapping.toMap(p))
        assertEquals("A", back.name)
        assertEquals(Gender.OTHER, back.gender)
        assertNull(back.age)
        assertNull(back.faceColors)
    }

    @Test
    fun `unknown gender defaults to FEMALE`() {
        assertEquals(Gender.FEMALE, ProfileMapping.fromMap(mapOf("gender" to "xxx")).gender)
    }

    @Test
    fun `partial colors are treated as no colors`() {
        assertNull(ProfileMapping.fromMap(mapOf("skin" to "1", "lip" to "2")).faceColors)
    }
}
