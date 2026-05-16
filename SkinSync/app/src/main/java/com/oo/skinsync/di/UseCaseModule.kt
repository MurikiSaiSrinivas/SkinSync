package com.oo.skinsync.di

import com.oo.skinsync.domain.ClearUserDataUseCase
import com.oo.skinsync.domain.DeleteLookUseCase
import com.oo.skinsync.domain.GetColorSuggestionUseCase
import com.oo.skinsync.domain.GetShoppingLinksUseCase
import com.oo.skinsync.domain.LooksRepository
import com.oo.skinsync.domain.MarkOnboardingSeenUseCase
import com.oo.skinsync.domain.ObserveLooksUseCase
import com.oo.skinsync.domain.ObserveOnboardingSeenUseCase
import com.oo.skinsync.domain.ObserveProfileUseCase
import com.oo.skinsync.domain.OnboardingRepository
import com.oo.skinsync.domain.ProfileRepository
import com.oo.skinsync.domain.SaveLookUseCase
import com.oo.skinsync.domain.SaveProfileUseCase
import com.oo.skinsync.domain.SelfieStore
import com.oo.skinsync.domain.ShoppingLinkRepository
import com.oo.skinsync.domain.SuggestionRepository
import com.oo.skinsync.domain.ToggleFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Domain use cases are plain Kotlin (`:domain` has no Hilt — rule #4, kept
 * iOS-shareable), so Hilt can't construct them via `@Inject`. This module
 * builds each from the repositories already bound in `:data`.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun observeProfile(repo: ProfileRepository) = ObserveProfileUseCase(repo)

    @Provides
    fun saveProfile(repo: ProfileRepository) = SaveProfileUseCase(repo)

    @Provides
    fun getColorSuggestion(repo: SuggestionRepository) = GetColorSuggestionUseCase(repo)

    @Provides
    fun getShoppingLinks(repo: ShoppingLinkRepository) = GetShoppingLinksUseCase(repo)

    @Provides
    fun clearUserData(profiles: ProfileRepository, selfies: SelfieStore) =
        ClearUserDataUseCase(profiles, selfies)

    @Provides
    fun observeLooks(repo: LooksRepository) = ObserveLooksUseCase(repo)

    @Provides
    fun saveLook(repo: LooksRepository) = SaveLookUseCase(repo)

    @Provides
    fun deleteLook(repo: LooksRepository) = DeleteLookUseCase(repo)

    @Provides
    fun toggleFavorite(repo: LooksRepository) = ToggleFavoriteUseCase(repo)

    @Provides
    fun observeOnboardingSeen(repo: OnboardingRepository) = ObserveOnboardingSeenUseCase(repo)

    @Provides
    fun markOnboardingSeen(repo: OnboardingRepository) = MarkOnboardingSeenUseCase(repo)
}
