package com.oo.skinsync.ml

import android.graphics.Bitmap
import com.oo.skinsync.domain.FaceColors

/** Extracts skin/lip/eye colors from a face bitmap. */
interface FaceColorExtractor {
    /** @return success with [FaceColors], or failure if no face / no usable pixels. */
    suspend fun extract(bitmap: Bitmap): Result<FaceColors>
}
