package com.oo.skinsync.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oo.skinsync.domain.MarkOnboardingSeenUseCase
import com.oo.skinsync.domain.ObserveOnboardingSeenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    observeOnboardingSeen: ObserveOnboardingSeenUseCase,
    private val markOnboardingSeen: MarkOnboardingSeenUseCase,
) : ViewModel() {

    /** null = still loading the flag; true/false = known. */
    val seen: StateFlow<Boolean?> =
        observeOnboardingSeen().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun completeOnboarding() {
        viewModelScope.launch { markOnboardingSeen() }
    }
}
