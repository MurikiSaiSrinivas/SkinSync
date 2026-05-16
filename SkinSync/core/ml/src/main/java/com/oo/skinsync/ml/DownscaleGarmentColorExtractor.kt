package com.oo.skinsync.ml

import android.graphics.Bitmap
import com.oo.skinsync.color.DominantColor
import com.oo.skinsync.color.GrayWorld
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downscales the garment photo (fast, robust to noise), white-balances, then
 * reuses the tested [DominantColor]. No MediaPipe — garments aren't faces.
 */
@Singleton
class DownscaleGarmentColorExtractor @Inject constructor() : GarmentColorExtractor {

    override suspend fun dominant(bitmap: Bitmap): Result<Int> =
        withContext(Dispatchers.Default) {
            runCatching {
                val src = if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap
                else bitmap.copy(Bitmap.Config.ARGB_8888, false)
                val scaled = Bitmap.createScaledBitmap(src, SAMPLE, SAMPLE, true)
                val pixels = IntArray(SAMPLE * SAMPLE)
                scaled.getPixels(pixels, 0, SAMPLE, 0, 0, SAMPLE, SAMPLE)
                DominantColor.of(GrayWorld.balance(pixels))
                    ?: error("Couldn't read the garment color. Try a closer, brighter shot.")
            }
        }

    private companion object {
        const val SAMPLE = 96
    }
}
