package com.oo.skinsync.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClearUserDataUseCaseTest {

    @Test
    fun `clears profile and deletes selfie`() = runTest {
        val profiles = FakeProfileRepository(Profile(name = "Sai", faceColors = faceColors))
        val selfies = FakeSelfieStore()
        ClearUserDataUseCase(profiles, selfies)()

        assertTrue(profiles.cleared)
        assertEquals(Profile(), profiles.state.value)
        assertTrue(selfies.deleted)
    }
}
