package com.oo.skinsync.feature.capture

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oo.skinsync.domain.ObserveProfileUseCase
import com.oo.skinsync.domain.SaveProfileUseCase
import com.oo.skinsync.domain.SelfieStore
import com.oo.skinsync.ml.FaceColorExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val extractor: FaceColorExtractor,
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val selfieStore: SelfieStore,
) : ViewModel() {

    private val _state = MutableStateFlow<CaptureState>(CaptureState.NeedsConsent)
    val state: StateFlow<CaptureState> = _state.asStateFlow()

    /** App-private path the camera writes to (single source of truth). */
    fun selfieFilePath(): String = selfieStore.selfieAbsolutePath()

    fun onConsentGranted() {
        if (_state.value is CaptureState.NeedsConsent) _state.value = CaptureState.Ready
    }

    fun onConsentDeclined() {
        _state.value = CaptureState.Error("Camera consent is required to scan your colors.")
    }

    fun retry() {
        _state.value = CaptureState.Ready
    }

    /** Called by the screen with the captured (already app-private) photo. */
    fun onPhotoCaptured(bitmap: Bitmap) {
        _state.value = CaptureState.Analyzing
        viewModelScope.launch {
            extractor.extract(bitmap)
                .onSuccess { colors ->
                    val current = observeProfile().first()
                    saveProfile(current.copy(faceColors = colors))
                    _state.value = CaptureState.Saved(colors)
                }
                .onFailure { e ->
                    _state.value = CaptureState.Error(e.message ?: "Could not read your colors.")
                }
        }
    }
}
