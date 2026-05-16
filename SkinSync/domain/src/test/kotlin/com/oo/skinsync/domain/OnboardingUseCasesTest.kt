package com.oo.skinsync.domain

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeOnboardingRepository : OnboardingRepository {
    val state = MutableStateFlow(false)
    override val seen = state
    override suspend fun markSeen() { state.value = true }
}

class OnboardingUseCasesTest {

    @Test
    fun `mark seen flips observed value`() = runTest {
        val repo = FakeOnboardingRepository()
        val observe = ObserveOnboardingSeenUseCase(repo)
        val mark = MarkOnboardingSeenUseCase(repo)

        observe().test {
            assertTrue(!awaitItem())
            mark()
            assertTrue(awaitItem())
        }
    }
}
