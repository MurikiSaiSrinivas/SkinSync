package com.oo.skinsync.domain

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileUseCasesTest {

    @Test
    fun `save then observe emits saved profile`() = runTest {
        val repo = FakeProfileRepository()
        val save = SaveProfileUseCase(repo)
        val observe = ObserveProfileUseCase(repo)

        save(Profile(name = "Sai", age = 24, gender = Gender.MALE))

        observe().test {
            val p = awaitItem()
            assertEquals("Sai", p.name)
            assertEquals(24, p.age)
            assertEquals(Gender.MALE, p.gender)
        }
    }

    @Test
    fun `shopping links use case delegates to repo`() {
        val useCase = GetShoppingLinksUseCase(FakeShoppingLinkRepository())
        val links = useCase("red gown")
        assertTrue(links.isNotEmpty())
        assertEquals("red gown", links.first().query)
    }
}
