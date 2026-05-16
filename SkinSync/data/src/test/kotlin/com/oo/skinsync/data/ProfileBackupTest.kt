package com.oo.skinsync.data

import com.oo.skinsync.domain.FaceColors
import com.oo.skinsync.domain.Gender
import com.oo.skinsync.domain.Profile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileBackupTest {

    @Test
    fun `round trips a full profile`() {
        val p = Profile("Sai", 24, Gender.MALE, FaceColors(1, 2, 3, 4))
        assertEquals(p, ProfileBackup.import(ProfileBackup.export(p)).getOrThrow())
    }

    @Test
    fun `round trips a profile without colors`() {
        val p = Profile("A", null, Gender.OTHER, null)
        assertEquals(p, ProfileBackup.import(ProfileBackup.export(p)).getOrThrow())
    }

    @Test
    fun `unknown gender falls back`() {
        val r = ProfileBackup.import("""{"name":"X","gender":"ZZZ"}""").getOrThrow()
        assertEquals(Gender.FEMALE, r.gender)
        assertEquals("X", r.name)
    }

    @Test
    fun `garbage fails gracefully`() {
        assertTrue(ProfileBackup.import("not json").isFailure)
    }
}
