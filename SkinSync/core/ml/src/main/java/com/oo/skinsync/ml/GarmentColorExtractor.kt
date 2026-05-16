package com.oo.skinsync.ml

import android.graphics.Bitmap

/** Extracts the dominant color of a photographed garment (no face mesh). */
interface GarmentColorExtractor {
    /** @return success with an opaque ARGB int, or failure if unreadable. */
    suspend fun dominant(bitmap: Bitmap): Result<Int>
}
