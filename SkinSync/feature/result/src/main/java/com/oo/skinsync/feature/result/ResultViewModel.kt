package com.oo.skinsync.feature.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oo.skinsync.domain.GetColorSuggestionUseCase
import com.oo.skinsync.domain.GetShoppingLinksUseCase
import com.oo.skinsync.domain.ObserveProfileUseCase
import com.oo.skinsync.domain.SaveLookUseCase
import com.oo.skinsync.domain.ShoppingLink
import com.oo.skinsync.domain.Suggestion
import com.oo.skinsync.domain.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val observeProfile: ObserveProfileUseCase,
    private val getColorSuggestion: GetColorSuggestionUseCase,
    private val getShoppingLinks: GetShoppingLinksUseCase,
    private val saveLook: SaveLookUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Suggestion>>(UiState.Empty)
    val state: StateFlow<UiState<Suggestion>> = _state.asStateFlow()

    /** Transient user feedback for the save action; null = nothing to show. */
    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    private var lastLocation: String = ""

    fun generate(location: String) {
        lastLocation = location
        _state.value = UiState.Loading
        viewModelScope.launch {
            val profile = observeProfile().first()
            getColorSuggestion(profile, location)
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Something went wrong.") }
        }
    }

    fun saveCurrent() {
        val s = _state.value
        if (s !is UiState.Success) return
        viewModelScope.launch {
            saveLook(lastLocation, s.data)
                .onSuccess { _saveMessage.value = "Look saved." }
                .onFailure { _saveMessage.value = it.message ?: "Couldn't save." }
        }
    }

    fun consumeSaveMessage() { _saveMessage.value = null }

    fun linksFor(query: String): List<ShoppingLink> = getShoppingLinks(query)
}
