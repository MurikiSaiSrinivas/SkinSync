package com.oo.skinsync.feature.capture

import com.oo.skinsync.domain.FaceColors

/** Capture flow state. Every case is rendered explicitly (rule #6). */
sealed interface CaptureState {
    /** Must explain why the camera is used and get consent before any capture. */
    data object NeedsConsent : CaptureState
    /** Consent given; camera preview shown. */
    data object Ready : CaptureState
    /** Photo taken, extracting colors (loading). */
    data object Analyzing : CaptureState
    /** Colors extracted and saved to the profile. */
    data class Saved(val colors: FaceColors) : CaptureState
    /** Recoverable error; user can retry. */
    data class Error(val message: String) : CaptureState
}
