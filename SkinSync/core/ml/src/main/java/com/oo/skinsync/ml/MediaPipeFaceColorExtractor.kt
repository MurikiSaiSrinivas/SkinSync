package com.oo.skinsync.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.oo.skinsync.color.GrayWorld
import com.oo.skinsync.domain.FaceColors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPipeFaceColorExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
) : FaceColorExtractor {

    private val landmarker: FaceLandmarker by lazy {
        val base = BaseOptions.builder().setModelAssetPath(MODEL_ASSET).build()
        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(base)
            .setRunningMode(RunningMode.IMAGE)
            .setNumFaces(1)
            .setMinFaceDetectionConfidence(0.5f)
            .setMinFacePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .build()
        FaceLandmarker.createFromOptions(context, options)
    }

    override suspend fun extract(bitmap: Bitmap): Result<FaceColors> =
        withContext(Dispatchers.Default) {
            runCatching {
                val argb = if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap
                else bitmap.copy(Bitmap.Config.ARGB_8888, false)

                val result = landmarker.detect(BitmapImageBuilder(argb).build())
                val faces = result.faceLandmarks()
                require(faces.isNotEmpty()) { "No face detected. Try better lighting." }

                val w = argb.width
                val h = argb.height
                val raw = IntArray(w * h)
                argb.getPixels(raw, 0, w, 0, 0, w, h)
                // B3: neutralize lighting color casts before sampling.
                val pixels = GrayWorld.balance(raw)

                val landmarks = faces[0].map {
                    FaceColorMapper.NLandmark(it.x(), it.y())
                }
                FaceColorMapper.colorsFrom(pixels, w, h, landmarks)
                    ?: error("Could not sample face colors. Try again.")
            }
        }

    fun close() = runCatching { landmarker.close() }

    private companion object {
        const val MODEL_ASSET = "face_landmarker.task"
    }
}
