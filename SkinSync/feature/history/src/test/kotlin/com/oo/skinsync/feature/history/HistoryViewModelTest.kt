package com.oo.skinsync.feature.history

import app.cash.turbine.test
import com.oo.skinsync.domain.ColorRec
import com.oo.skinsync.domain.DeleteLookUseCase
import com.oo.skinsync.domain.LooksRepository
import com.oo.skinsync.domain.ObserveLooksUseCase
import com.oo.skinsync.domain.SavedLook
import com.oo.skinsync.domain.Suggestion
import com.oo.skinsync.domain.UiState
import kotlinx.coroutines.Dispatchers
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

private class FakeLooks : LooksRepository {
    val flow = MutableStateFlow<List<SavedLook>>(emptyList())
    override fun observeLooks() = flow
    override suspend fun save(location: String, suggestion: Suggestion) = Result.success(Unit)
    override suspend fun delete(id: String): Result<Unit> {
        flow.value = flow.value.filterNot { it.id == id }; return Result.success(Unit)
    }
    override suspend fun setFavorite(id: String, favorite: Boolean): Result<Unit> {
        flow.value = flow.value.map { if (it.id == id) it.copy(favorite = favorite) else it }
        return Result.success(Unit)
    }
}

class HistoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private val look = SavedLook(
        "1", 1L, "Goa",
        Suggestion("Soft Autumn", listOf(ColorRec("#A23BA0", "Orchid", "warm", listOf("orchid dress")))),
    )

    private fun vm(repo: FakeLooks) = HistoryViewModel(
        ObserveLooksUseCase(repo),
        DeleteLookUseCase(repo),
        com.oo.skinsync.domain.ToggleFavoriteUseCase(repo),
    )

    @Test
    fun `empty list maps to Empty`() = runTest(dispatcher) {
        vm(FakeLooks()).state.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(UiState.Empty, awaitItem())
        }
    }

    @Test
    fun `non-empty list maps to Success`() = runTest(dispatcher) {
        val repo = FakeLooks().apply { flow.value = listOf(look) }
        vm(repo).state.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            val s = awaitItem()
            assertTrue(s is UiState.Success)
            assertEquals("Goa", (s as UiState.Success).data.first().location)
        }
    }

    @Test
    fun `delete removes the look`() = runTest(dispatcher) {
        val repo = FakeLooks().apply { flow.value = listOf(look) }
        val v = vm(repo)
        advanceUntilIdle()
        v.delete("1")
        advanceUntilIdle()
        assertTrue(repo.flow.value.isEmpty())
    }

    @Test
    fun `favorites are sorted first`() = runTest(dispatcher) {
        val older = look.copy(id = "1", createdAt = 1L, favorite = false)
        val newerFav = look.copy(id = "2", createdAt = 2L, favorite = true)
        val newestPlain = look.copy(id = "3", createdAt = 3L, favorite = false)
        val repo = FakeLooks().apply { flow.value = listOf(older, newerFav, newestPlain) }
        vm(repo).state.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            val data = (awaitItem() as UiState.Success).data
            assertEquals("2", data[0].id) // favorite first
            assertEquals("3", data[1].id) // then newest
            assertEquals("1", data[2].id)
        }
    }

    @Test
    fun `toggle favorite updates the repository`() = runTest(dispatcher) {
        val repo = FakeLooks().apply { flow.value = listOf(look) }
        val v = vm(repo)
        advanceUntilIdle()
        v.toggleFavorite(look)
        advanceUntilIdle()
        assertTrue(repo.flow.value.first().favorite)
    }
}
