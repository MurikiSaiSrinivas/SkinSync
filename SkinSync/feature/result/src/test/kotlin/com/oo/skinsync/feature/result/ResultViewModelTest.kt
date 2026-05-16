package com.oo.skinsync.feature.result

import app.cash.turbine.test
import com.oo.skinsync.domain.ColorRec
import com.oo.skinsync.domain.FaceColors
import com.oo.skinsync.domain.GetColorSuggestionUseCase
import com.oo.skinsync.domain.GetShoppingLinksUseCase
import com.oo.skinsync.domain.ObserveProfileUseCase
import com.oo.skinsync.domain.Profile
import com.oo.skinsync.domain.ProfileRepository
import com.oo.skinsync.domain.ShoppingLink
import com.oo.skinsync.domain.ShoppingLinkRepository
import com.oo.skinsync.domain.Suggestion
import com.oo.skinsync.domain.SuggestionRepository
import com.oo.skinsync.domain.UiState
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

private val colors = FaceColors(1, 2, 3, 4)

private class FakeProfileRepo : ProfileRepository {
    val s = MutableStateFlow(Profile("A", 25, faceColors = colors))
    override val profile: Flow<Profile> = s
    override suspend fun save(profile: Profile) {}
    override suspend fun clear() {}
}

private class FakeSuggestions(var result: Result<Suggestion>) : SuggestionRepository {
    override suspend fun getSuggestion(profile: Profile, location: String) = result
}

private class FakeLinks : ShoppingLinkRepository {
    override fun linksFor(query: String) = listOf(ShoppingLink("Amazon", query, "https://x/$query"))
}

private class FakeLooks : com.oo.skinsync.domain.LooksRepository {
    val saved = mutableListOf<Pair<String, Suggestion>>()
    override fun observeLooks() = kotlinx.coroutines.flow.flowOf(emptyList<com.oo.skinsync.domain.SavedLook>())
    override suspend fun save(location: String, suggestion: Suggestion): Result<Unit> {
        saved += location to suggestion; return Result.success(Unit)
    }
    override suspend fun delete(id: String): Result<Unit> = Result.success(Unit)
    override suspend fun setFavorite(id: String, favorite: Boolean): Result<Unit> = Result.success(Unit)
}

class ResultViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private val looks = FakeLooks()

    private fun vm(result: Result<Suggestion>) = ResultViewModel(
        ObserveProfileUseCase(FakeProfileRepo()),
        GetColorSuggestionUseCase(FakeSuggestions(result)),
        GetShoppingLinksUseCase(FakeLinks()),
        com.oo.skinsync.domain.SaveLookUseCase(looks),
    )

    private val ok = Suggestion("Soft Autumn", listOf(ColorRec("#A23BA0", "Orchid", "warm", listOf("orchid dress"))))

    @Test
    fun `starts empty`() {
        assertEquals(UiState.Empty, vm(Result.success(ok)).state.value)
    }

    @Test
    fun `success emits Loading then Success`() = runTest(dispatcher) {
        val v = vm(Result.success(ok))
        v.state.test {
            assertEquals(UiState.Empty, awaitItem())
            v.generate("Goa")
            assertEquals(UiState.Loading, awaitItem())
            val s = awaitItem()
            assertTrue(s is UiState.Success)
            assertEquals("Soft Autumn", (s as UiState.Success).data.seasonalType)
        }
    }

    @Test
    fun `failure emits Error`() = runTest(dispatcher) {
        val v = vm(Result.failure(RuntimeException("network down")))
        v.generate("Paris")
        v.state.test {
            var last = awaitItem()
            while (last !is UiState.Error) last = awaitItem()
            assertEquals("network down", (last as UiState.Error).message)
        }
    }

    @Test
    fun `linksFor delegates to use case`() {
        val links = vm(Result.success(ok)).linksFor("red gown")
        assertEquals(1, links.size)
        assertEquals("red gown", links.first().query)
    }

    @Test
    fun `saveCurrent persists the shown suggestion with its location`() = runTest(dispatcher) {
        val v = vm(Result.success(ok))
        v.generate("Goa")
        v.state.test {
            var s = awaitItem()
            while (s !is UiState.Success) s = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        v.saveCurrent()
        kotlinx.coroutines.test.advanceUntilIdle()
        assertEquals(1, looks.saved.size)
        assertEquals("Goa", looks.saved.first().first)
        assertEquals("Look saved.", v.saveMessage.value)
    }

    @Test
    fun `saveCurrent does nothing when not successful`() = runTest(dispatcher) {
        val v = vm(Result.success(ok))
        v.saveCurrent()
        kotlinx.coroutines.test.advanceUntilIdle()
        assertEquals(0, looks.saved.size)
    }
}
