package com.oo.skinsync.feature.profile

import app.cash.turbine.test
import com.oo.skinsync.domain.ClearUserDataUseCase
import com.oo.skinsync.domain.FaceColors
import com.oo.skinsync.domain.Gender
import com.oo.skinsync.domain.ObserveProfileUseCase
import com.oo.skinsync.domain.Profile
import com.oo.skinsync.domain.ProfileRepository
import com.oo.skinsync.domain.SaveProfileUseCase
import com.oo.skinsync.domain.SelfieStore
import com.oo.skinsync.domain.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeProfileRepo(initial: Profile = Profile()) : ProfileRepository {
    val state = MutableStateFlow(initial)
    override val profile: Flow<Profile> = state
    var cleared = false
    override suspend fun save(profile: Profile) { state.value = profile }
    override suspend fun clear() { cleared = true; state.value = Profile() }
}

private class FakeSelfieStore : SelfieStore {
    var deleted = false
    override fun selfieAbsolutePath() = "/tmp/selfie.jpg"
    override fun deleteSelfie() { deleted = true }
}

class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(repo: FakeProfileRepo, selfie: FakeSelfieStore = FakeSelfieStore()) =
        ProfileViewModel(
            ObserveProfileUseCase(repo),
            SaveProfileUseCase(repo),
            ClearUserDataUseCase(repo, selfie),
            selfie,
        )

    @Test
    fun `seeds draft from stored profile`() = runTest(dispatcher) {
        val repo = FakeProfileRepo(Profile("Sai", 24, Gender.MALE, FaceColors(1, 2, 3, 4)))
        val v = vm(repo)
        v.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            val s = awaitItem()
            assertTrue(s is UiState.Success)
            assertEquals("Sai", (s as UiState.Success).data.name)
        }
    }

    @Test
    fun `edits then save persists to repository`() = runTest(dispatcher) {
        val repo = FakeProfileRepo(Profile("Old"))
        val v = vm(repo)
        advanceUntilIdle()
        v.onNameChange("New")
        v.onAgeChange("30")
        v.onGenderChange(Gender.OTHER)
        v.save()
        advanceUntilIdle()
        assertEquals(Profile("New", 30, Gender.OTHER, null), repo.state.value)
    }

    @Test
    fun `delete my data clears repo, selfie and draft`() = runTest(dispatcher) {
        val repo = FakeProfileRepo(Profile("Sai", 24))
        val selfie = FakeSelfieStore()
        val v = vm(repo, selfie)
        advanceUntilIdle()
        v.deleteMyData()
        advanceUntilIdle()
        assertTrue(repo.cleared)
        assertTrue(selfie.deleted)
        assertEquals(Profile(), repo.state.value)
    }

    @Test
    fun `exposes selfie path`() {
        assertEquals("/tmp/selfie.jpg", vm(FakeProfileRepo()).selfiePath())
    }
}
