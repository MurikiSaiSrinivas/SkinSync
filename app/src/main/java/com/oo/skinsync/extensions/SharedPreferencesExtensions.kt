package com.oo.skinsync.extensions


import android.content.SharedPreferences
import com.oo.skinsync.models.FaceAnalysisResult

fun SharedPreferences.saveFaceAnalysisResult(result: FaceAnalysisResult) {
    edit().apply {
        putInt("face_color", result.cheekColor)
        putInt("lip_color", result.lipColor)
        putInt("left_eye_color", result.leftEyeColor)
        putInt("right_eye_color", result.rightEyeColor)
        apply()
    }
}