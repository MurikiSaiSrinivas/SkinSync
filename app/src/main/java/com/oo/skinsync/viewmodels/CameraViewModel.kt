package com.oo.skinsync.viewmodels

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.oo.skinsync.constants.FacialLandmarkConstants
import com.oo.skinsync.models.CameraState
import com.oo.skinsync.models.FaceAnalysisResult
import com.oo.skinsync.utils.ColorAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Preview)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _faceAnalysisResult = MutableStateFlow<FaceAnalysisResult?>(null)
    val faceAnalysisResult: StateFlow<FaceAnalysisResult?> = _faceAnalysisResult.asStateFlow()

    fun setCameraState(state: CameraState) {
        _cameraState.value = state
    }

    fun processFaceLandmarks(landmarks: List<NormalizedLandmark>, bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val cheekColor = ColorAnalyzer.findDominantColor(
                    getPixelsInPolygon(bitmap, landmarks, FacialLandmarkConstants.CHEEK_INDICES)
                )
                val lipColor = ColorAnalyzer.findDominantColor(
                    getPixelsInPolygon(bitmap, landmarks, FacialLandmarkConstants.LIP_INDICES)
                )
                val leftEyeColor = ColorAnalyzer.findDominantColor(
                    getPixelsInPolygon(bitmap, landmarks, FacialLandmarkConstants.LEFT_EYE_INDICES)
                )
                val rightEyeColor = ColorAnalyzer.findDominantColor(
                    getPixelsInPolygon(bitmap, landmarks, FacialLandmarkConstants.RIGHT_EYE_INDICES)
                )

                _faceAnalysisResult.value = FaceAnalysisResult(
                    cheekColor = cheekColor,
                    lipColor = lipColor,
                    leftEyeColor = leftEyeColor,
                    rightEyeColor = rightEyeColor
                )
            } catch (e: Exception) {
                _cameraState.value = CameraState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun getPixelsInPolygon(
        image: Bitmap,
        landmarks: List<NormalizedLandmark>,
        indices: List<Int>
    ): List<Int> {
        val width = image.width
        val height = image.height
        val pixels = IntArray(width * height)

        // Get pixel data from the image
        image.getPixels(pixels, 0, width, 0, 0, width, height)

        // Create a path for the polygon
        val path = Path()
        if (indices.isNotEmpty()) {
            // Move to the first landmark
            val firstPoint = landmarks[indices[0]]
            path.moveTo(firstPoint.x() * width, firstPoint.y() * height)

            // Draw lines to the other landmarks
            for (i in 1 until indices.size) {
                val point = landmarks[indices[i]]
                path.lineTo(point.x() * width, point.y() * height)
            }
            path.close() // Close the polygon
        }

        // Create a new bitmap to hold the masked image
        val maskedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(maskedBitmap)
        val paint = Paint().apply {
            color = Color.BLACK // Set a color to fill the polygon
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Draw the polygon on the canvas
        canvas.drawPath(path, paint)

        val resultPixels = mutableListOf<Int>()
        for (y in 0 until height) {
            for (x in 0 until width) {
                // Check if the pixel is within the polygon by using the mask color
                if (maskedBitmap.getPixel(x, y) == Color.BLACK) { // Inside polygon
                    resultPixels.add(pixels[y * width + x]) // Copy the pixel value
                }
            }
        }

        // Log the size of resultPixels for debugging
        Log.d("PolygonPixels", "Number of pixels inside polygon: ${resultPixels.size}")

        return resultPixels
    }
}