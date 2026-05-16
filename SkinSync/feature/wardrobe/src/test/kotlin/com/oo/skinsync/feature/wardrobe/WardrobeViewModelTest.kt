package com.oo.skinsync.feature.wardrobe

import android.graphics.Bitmap
import app.cash.turbine.test
import com.oo.skinsync.domain.GetShoppingLinksUseCase
import com.oo.skinsync.domain.ShoppingLink
import com.oo.skinsync.domain.ShoppingLinkRepository
import com.oo.skinsync.ml.GarmentColorExtractor
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeGarment(var result: Result<Int>) : GarmentColorExtractor {
    override suspend fun dominant(bitmap: Bitmap): Result<Int> = result
}

private class FakeLinks : ShoppingLinkRepository {
    override fun linksFor(query: String) = listOf(ShoppingLink("Amazon", query, "https://x/$query"))
}

class WardrobeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val bitmap = mockk<Bitmap>(relaxed = true)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(result: Result<Int>) =
        WardrobeViewModel(FakeGarment(result), GetShoppingLinksUseCase(FakeLinks()))

    @Test
    fun `starts needing consent then ready`() {
        val v = vm(Result.success(0xFF3B82F6.toInt()))
        assertTrue(v.state.value is WardrobeState.NeedsConsent)
        v.onConsentGranted()
        assertTrue(v.state.value is WardrobeState.Ready)
    }

    @Test
    fun `capture produces a matching palette`() = runTest(dispatcher) {
        val v = vm(Result.success(0xFF1565C0.toInt()))
        v.onConsentGranted()
        v.state.test {
            assertTrue(awaitItem() is WardrobeState.Ready)
            v.onPhotoCaptured(bitmap)
            assertTrue(awaitItem() is WardrobeState.Analyzing)
            val r = awaitItem()
            assertTrue(r is WardrobeState.Result)
            r as WardrobeState.Result
            assertEquals("blue", r.colorName)
            assertTrue(r.matches.isNotEmpty())
        }
    }

    @Test
    fun `extractor failure surfaces an error`() = runTest(dispatcher) {
        val v = vm(Result.failure(IllegalStateException("too dark")))
        v.onConsentGranted()
        v.onPhotoCaptured(bitmap)
        v.state.test {
            var s = awaitItem()
            while (s !is WardrobeState.Error) s = awaitItem()
            assertEquals("too dark", (s as WardrobeState.Error).message)
        }
    }

    @Test
    fun `links use the matched color name`() {
        val links = vm(Result.success(0xFF2E7D32.toInt())).linksFor(0xFF2E7D32.toInt())
        assertTrue(links.first().query.contains("green"))
    }
}
