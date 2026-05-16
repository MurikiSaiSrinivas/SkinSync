package com.oo.skinsync.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oo.skinsync.domain.ClearUserDataUseCase
import com.oo.skinsync.domain.Gender
import com.oo.skinsync.domain.ObserveProfileUseCase
import com.oo.skinsync.domain.Profile
import com.oo.skinsync.domain.SaveProfileUseCase
import com.oo.skinsync.domain.SelfieStore
import com.oo.skinsync.domain.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val clearUserData: ClearUserDataUseCase,
    private val selfieStore: SelfieStore,
) : ViewModel() {

    /** Editable draft, seeded once from storage. */
    private val draft = MutableStateFlow<Profile?>(null)

    val uiState: StateFlow<UiState<Profile>> =
        draft.map { if (it == null) UiState.Loading else UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        viewModelScope.launch {
            observeProfile().collect { stored ->
                if (draft.value == null) draft.value = stored
            }
        }
    }

    fun onNameChange(value: String) = draft.update { it?.copy(name = value) }
    fun onAgeChange(value: String) = draft.update { it?.copy(age = value.toIntOrNull()) }
    fun onGenderChange(value: Gender) = draft.update { it?.copy(gender = value) }

    fun save() {
        val current = draft.value ?: return
        viewModelScope.launch { saveProfile(current) }
    }

    fun deleteMyData() {
        viewModelScope.launch {
            clearUserData()
            draft.value = Profile()
        }
    }

    fun selfiePath(): String = selfieStore.selfieAbsolutePath()
}
