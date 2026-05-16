package com.oo.skinsync.domain

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LooksUseCasesTest {

    private val sample = Suggestion("Soft Autumn", listOf(ColorRec("#A23BA0", "Orchid", "warm", listOf("orchid dress"))))

    @Test
    fun `save then observe shows the look`() = runTest {
        val repo = FakeLooksRepository()
        SaveLookUseCase(repo)("Goa", sample)
        ObserveLooksUseCase(repo)().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Goa", list.first().location)
        }
    }

    @Test
    fun `saving an empty palette fails and stores nothing`() = runTest {
        val repo = FakeLooksRepository()
        val r = SaveLookUseCase(repo)("X", Suggestion("T", emptyList()))
        assertTrue(r.isFailure)
        assertEquals(0, repo.looks.value.size)
    }

    @Test
    fun `delete removes the look`() = runTest {
        val repo = FakeLooksRepository()
        SaveLookUseCase(repo)("A", sample)
        val id = repo.looks.value.first().id
        DeleteLookUseCase(repo)(id)
        assertEquals(0, repo.looks.value.size)
    }

    @Test
    fun `toggle favorite flips the flag`() = runTest {
        val repo = FakeLooksRepository()
        SaveLookUseCase(repo)("A", sample)
        val id = repo.looks.value.first().id
        ToggleFavoriteUseCase(repo)(id, true)
        assertTrue(repo.looks.value.first().favorite)
        ToggleFavoriteUseCase(repo)(id, false)
        assertTrue(!repo.looks.value.first().favorite)
    }
}
