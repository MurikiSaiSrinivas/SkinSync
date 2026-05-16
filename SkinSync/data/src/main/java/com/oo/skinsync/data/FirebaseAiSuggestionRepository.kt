package com.oo.skinsync.data

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.oo.skinsync.domain.Profile
import com.oo.skinsync.domain.Suggestion
import com.oo.skinsync.domain.SuggestionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calls Gemini through **Firebase AI Logic** — the API key is held by Firebase,
 * never shipped in the APK (rule #2). The testable parts (prompt, parsing) live
 * in [PromptBuilder] / [SuggestionParser]; this class only does the I/O.
 */
@Singleton
class FirebaseAiSuggestionRepository @Inject constructor() : SuggestionRepository {

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODEL,
            generationConfig = generationConfig { responseMimeType = "application/json" },
        )
    }

    override suspend fun getSuggestion(profile: Profile, location: String): Result<Suggestion> =
        withContext(Dispatchers.IO) {
            runCatching {
                val prompt = PromptBuilder.build(profile, location)
                val text = model.generateContent(prompt).text
                    ?: error("The AI returned an empty response. Try again.")
                SuggestionParser.parse(text).getOrThrow()
            }
        }

    private companion object {
        const val MODEL = "gemini-2.0-flash"
    }
}
