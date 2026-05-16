package com.oo.skinsync.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oo.skinsync.domain.DeleteLookUseCase
import com.oo.skinsync.domain.ObserveLooksUseCase
import com.oo.skinsync.domain.SavedLook
import com.oo.skinsync.domain.ToggleFavoriteUseCase
import com.oo.skinsync.domain.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeLooks: ObserveLooksUseCase,
    private val deleteLook: DeleteLookUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    val state: StateFlow<UiState<List<SavedLook>>> =
        observeLooks()
            // Favorites first, then newest first.
            .map { looks -> looks.sortedWith(compareByDescending<SavedLook> { it.favorite }.thenByDescending { it.createdAt }) }
            .map { if (it.isEmpty()) UiState.Empty else UiState.Success(it) }
            .catch { emit(UiState.Error(it.message ?: "Couldn't load saved looks.")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun delete(id: String) {
        viewModelScope.launch { deleteLook(id) }
    }

    fun toggleFavorite(look: SavedLook) {
        viewModelScope.launch { toggleFavorite(look.id, !look.favorite) }
    }
}
