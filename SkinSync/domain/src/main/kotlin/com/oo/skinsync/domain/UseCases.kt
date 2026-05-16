package com.oo.skinsync.domain

import kotlinx.coroutines.flow.Flow

/** Get an AI color suggestion for the current profile + a location. */
class GetColorSuggestionUseCase(
    private val suggestions: SuggestionRepository,
) {
    suspend operator fun invoke(profile: Profile, location: String): Result<Suggestion> {
        val trimmed = location.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a photoshoot location."))
        }
        if (profile.faceColors == null) {
            return Result.failure(IllegalStateException("Capture your face colors first."))
        }
        return suggestions.getSuggestion(profile, trimmed)
    }
}

/** Observe the saved profile. */
class ObserveProfileUseCase(private val repo: ProfileRepository) {
    operator fun invoke(): Flow<Profile> = repo.profile
}

/** Save the profile. */
class SaveProfileUseCase(private val repo: ProfileRepository) {
    suspend operator fun invoke(profile: Profile) = repo.save(profile)
}

/** Build shopping deep links for a chosen color's outfit query. */
class GetShoppingLinksUseCase(private val repo: ShoppingLinkRepository) {
    operator fun invoke(query: String): List<ShoppingLink> = repo.linksFor(query)
}

/** Delete-my-data: wipes the saved profile and the on-device selfie. */
class ClearUserDataUseCase(
    private val profiles: ProfileRepository,
    private val selfies: SelfieStore,
) {
    suspend operator fun invoke() {
        profiles.clear()
        selfies.deleteSelfie()
    }
}

/** Observe the user's saved looks. */
class ObserveLooksUseCase(private val repo: LooksRepository) {
    operator fun invoke() = repo.observeLooks()
}

/** Save the current recommendation as a look. */
class SaveLookUseCase(private val repo: LooksRepository) {
    suspend operator fun invoke(location: String, suggestion: Suggestion): Result<Unit> {
        if (suggestion.palette.isEmpty()) {
            return Result.failure(IllegalArgumentException("Nothing to save yet."))
        }
        return repo.save(location, suggestion)
    }
}

/** Delete a saved look. */
class DeleteLookUseCase(private val repo: LooksRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repo.delete(id)
}

/** Star / unstar a saved look. */
class ToggleFavoriteUseCase(private val repo: LooksRepository) {
    suspend operator fun invoke(id: String, favorite: Boolean): Result<Unit> =
        repo.setFavorite(id, favorite)
}

/** Observe whether onboarding has been seen. */
class ObserveOnboardingSeenUseCase(private val repo: OnboardingRepository) {
    operator fun invoke() = repo.seen
}

/** Mark onboarding as completed. */
class MarkOnboardingSeenUseCase(private val repo: OnboardingRepository) {
    suspend operator fun invoke() = repo.markSeen()
}
