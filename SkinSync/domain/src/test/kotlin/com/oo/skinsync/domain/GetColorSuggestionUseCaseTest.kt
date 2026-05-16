package com.oo.skinsync.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetColorSuggestionUseCaseTest {

    private val repo = FakeSuggestionRepository()
    private val useCase = GetColorSuggestionUseCase(repo)
    private val validProfile = Profile(name = "A", age = 23, faceColors = faceColors)

    @Test
    fun `blank location fails`() = runTest {
        val r = useCase(validProfile, "   ")
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `missing face colors fails`() = runTest {
        val r = useCase(Profile(name = "A"), "Paris")
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `valid input trims location and returns suggestion`() = runTest {
        val r = useCase(validProfile, "  Paris  ")
        assertTrue(r.isSuccess)
        assertEquals("Paris", repo.lastLocation)
        assertEquals("Soft Autumn", r.getOrNull()?.seasonalType)
    }

    @Test
    fun `repository failure is propagated`() = runTest {
        repo.result = Result.failure(RuntimeException("network"))
        val r = useCase(validProfile, "Tokyo")
        assertTrue(r.isFailure)
        assertEquals("network", r.exceptionOrNull()?.message)
    }
}
