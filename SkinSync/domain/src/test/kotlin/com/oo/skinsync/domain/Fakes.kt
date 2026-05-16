package com.oo.skinsync.domain

import kotlinx.coroutines.flow.MutableStateFlow

class FakeSuggestionRepository(
    var result: Result<Suggestion> = Result.success(
        Suggestion("Soft Autumn", listOf(ColorRec("#A23BA0", "Orchid", "warm", listOf("orchid dress"))))
    ),
) : SuggestionRepository {
    var lastProfile: Profile? = null
    var lastLocation: String? = null
    override suspend fun getSuggestion(profile: Profile, location: String): Result<Suggestion> {
        lastProfile = profile
        lastLocation = location
        return result
    }
}

class FakeProfileRepository(initial: Profile = Profile()) : ProfileRepository {
    val state = MutableStateFlow(initial)
    override val profile = state
    var cleared = false
    override suspend fun save(profile: Profile) { state.value = profile }
    override suspend fun clear() { cleared = true; state.value = Profile() }
}

class FakeShoppingLinkRepository : ShoppingLinkRepository {
    override fun linksFor(query: String): List<ShoppingLink> =
        listOf(ShoppingLink("Amazon", query, "https://example.com/$query"))
}

class FakeSelfieStore : SelfieStore {
    var deleted = false
    override fun selfieAbsolutePath() = "/tmp/selfie.jpg"
    override fun deleteSelfie() { deleted = true }
}

class FakeLooksRepository : LooksRepository {
    val looks = kotlinx.coroutines.flow.MutableStateFlow<List<SavedLook>>(emptyList())
    var saveResult: Result<Unit> = Result.success(Unit)
    override fun observeLooks() = looks
    override suspend fun save(location: String, suggestion: Suggestion): Result<Unit> {
        if (saveResult.isSuccess) {
            looks.value = looks.value + SavedLook("id${looks.value.size}", looks.value.size.toLong(), location, suggestion)
        }
        return saveResult
    }
    override suspend fun delete(id: String): Result<Unit> {
        looks.value = looks.value.filterNot { it.id == id }
        return Result.success(Unit)
    }
    override suspend fun setFavorite(id: String, favorite: Boolean): Result<Unit> {
        looks.value = looks.value.map { if (it.id == id) it.copy(favorite = favorite) else it }
        return Result.success(Unit)
    }
}

val faceColors = FaceColors(skin = 0xFFE0B0A0.toInt(), lip = 0xFFB04050.toInt(), leftEye = 0xFF604030.toInt(), rightEye = 0xFF604030.toInt())
