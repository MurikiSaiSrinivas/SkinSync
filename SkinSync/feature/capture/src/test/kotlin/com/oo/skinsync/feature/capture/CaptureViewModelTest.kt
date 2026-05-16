package com.oo.skinsync.feature.capture

import android.graphics.Bitmap
import app.cash.turbine.test
import com.oo.skinsync.domain.FaceColors
import com.oo.skinsync.domain.ObserveProfileUseCase
import com.oo.skinsync.domain.Profile
import com.oo.skinsync.domain.ProfileRepository
import com.oo.skinsync.domain.SaveProfileUseCase
import com.oo.skinsync.domain.SelfieStore
import com.oo.skinsync.ml.FaceColorExtractor
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeProfileRepo : ProfileRepository {
    val state = MutableStateFlow(Profile())
    override val profile: Flow<Profile> = state
    override suspend fun save(profile: Profile) { state.value = profile }
    override suspend fun clear() { state.value = Profile() }
}

private class FakeExtractor(var result: Result<FaceColors>) : FaceColorExtractor {
    override suspend fun extract(bitmap: Bitmap): Result<FaceColors> = result
}

private class FakeSelfieStore : SelfieStore {
    var deleted = false
    override fun selfieAbsolutePath() = "/tmp/selfie.jpg"
    override fun deleteSelfie() { deleted = true }
}

class CaptureViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeProfileRepo()
    private val bitmap = mockk<Bitmap>(relaxed = true)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(extractor: FaceColorExtractor) = CaptureViewModel(
        extractor, ObserveProfileUseCase(repo), SaveProfileUseCase(repo), FakeSelfieStore(),
    )

    @Test
    fun `exposes the selfie store path`() {
        assertEquals("/tmp/selfie.jpg", vm(FakeExtractor(Result.success(FaceColors(1, 2, 3, 4)))).selfieFilePath())
    }

    @Test
    fun `starts needing consent`() {
        assertTrue(vm(FakeExtractor(Result.success(FaceColors(1, 2, 3, 4)))).state.value is CaptureState.NeedsConsent)
    }

    @Test
    fun `consent granted moves to ready`() {
        val v = vm(FakeExtractor(Result.success(FaceColors(1, 2, 3, 4))))
        v.onConsentGranted()
        assertTrue(v.state.value is CaptureState.Ready)
    }

    @Test
    fun `declined consent is an error`() {
        val v = vm(FakeExtractor(Result.success(FaceColors(1, 2, 3, 4))))
        v.onConsentDeclined()
        assertTrue(v.state.value is CaptureState.Error)
    }

    @Test
    fun `capture success extracts, saves to profile, and emits Saved`() = runTest(dispatcher) {
        val colors = FaceColors(10, 20, 30, 40)
        val v = vm(FakeExtractor(Result.success(colors)))
        v.onConsentGranted()
        v.state.test {
            assertTrue(awaitItem() is CaptureState.Ready)
            v.onPhotoCaptured(bitmap)
            assertTrue(awaitItem() is CaptureState.Analyzing)
            val saved = awaitItem()
            assertTrue(saved is CaptureState.Saved)
            assertEquals(colors, (saved as CaptureState.Saved).colors)
        }
        assertEquals(colors, repo.state.value.faceColors)
    }

    @Test
    fun `capture failure emits Error`() = runTest(dispatcher) {
        val v = vm(FakeExtractor(Result.failure(IllegalStateException("No face detected."))))
        v.onConsentGranted()
        v.onPhotoCaptured(bitmap)
        v.state.test {
            // drains to terminal Error
            var last = awaitItem()
            while (last !is CaptureState.Error) last = awaitItem()
            assertEquals("No face detected.", (last as CaptureState.Error).message)
        }
    }
}
