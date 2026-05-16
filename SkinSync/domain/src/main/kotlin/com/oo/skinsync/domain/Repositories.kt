package com.oo.skinsync.domain

import kotlinx.coroutines.flow.Flow

/** Profile persistence. Impl in :data (DataStore + Firestore). */
interface ProfileRepository {
    val profile: Flow<Profile>
    suspend fun save(profile: Profile)
    suspend fun clear()
}

/** AI color recommendation. Impl in :data (Firebase AI Logic). */
interface SuggestionRepository {
    /** @return [Result] so callers map to [UiState] without exceptions leaking to UI. */
    suspend fun getSuggestion(profile: Profile, location: String): Result<Suggestion>
}

/** Builds retailer search deep links. Impl in :data (pure URL building). */
interface ShoppingLinkRepository {
    fun linksFor(query: String): List<ShoppingLink>
}

/**
 * The single source of truth for where the app-private selfie lives, so
 * capture (write) and delete-my-data (remove) never disagree. Impl in :data.
 */
interface SelfieStore {
    /** Absolute path of the app-private selfie file (never the public gallery). */
    fun selfieAbsolutePath(): String
    /** Deletes the selfie if present. Safe to call when absent. */
    fun deleteSelfie()
}

/** Saved looks history. Impl in :data (Firestore, per anonymous user). */
interface LooksRepository {
    fun observeLooks(): kotlinx.coroutines.flow.Flow<List<SavedLook>>
    suspend fun save(location: String, suggestion: Suggestion): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
    suspend fun setFavorite(id: String, favorite: Boolean): Result<Unit>
}

/** Whether the user has seen first-run onboarding. Impl in :data (DataStore). */
interface OnboardingRepository {
    val seen: kotlinx.coroutines.flow.Flow<Boolean>
    suspend fun markSeen()
}
