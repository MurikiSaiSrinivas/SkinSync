package com.oo.skinsync.feature.wardrobe

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oo.skinsync.color.ColorNamer
import com.oo.skinsync.color.Harmony
import com.oo.skinsync.domain.GetShoppingLinksUseCase
import com.oo.skinsync.domain.ShoppingLink
import com.oo.skinsync.ml.GarmentColorExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val extractor: GarmentColorExtractor,
    private val getShoppingLinks: GetShoppingLinksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<WardrobeState>(WardrobeState.NeedsConsent)
    val state: StateFlow<WardrobeState> = _state.asStateFlow()

    fun onConsentGranted() {
        if (_state.value is WardrobeState.NeedsConsent) _state.value = WardrobeState.Ready
    }

    fun onConsentDeclined() {
        _state.value = WardrobeState.Error("Camera consent is required.")
    }

    fun retry() { _state.value = WardrobeState.Ready }

    fun onPhotoCaptured(bitmap: Bitmap) {
        _state.value = WardrobeState.Analyzing
        viewModelScope.launch {
            extractor.dominant(bitmap)
                .onSuccess { base ->
                    _state.value = WardrobeState.Result(
                        baseColor = base,
                        matches = Harmony.list(base).drop(1), // exclude the garment itself
                        colorName = ColorNamer.name(base),
                    )
                }
                .onFailure {
                    _state.value = WardrobeState.Error(it.message ?: "Couldn't read the color.")
                }
        }
    }

    /** Shopping links for a chosen matching color. */
    fun linksFor(matchColor: Int): List<ShoppingLink> =
        getShoppingLinks("${ColorNamer.name(matchColor)} outfit")
}
