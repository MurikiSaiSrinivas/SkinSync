package com.oo.skinsync.models

sealed class CameraState {
    object Preview : CameraState()
    object ImageCapture : CameraState()
    data class Error(val message: String) : CameraState()
}