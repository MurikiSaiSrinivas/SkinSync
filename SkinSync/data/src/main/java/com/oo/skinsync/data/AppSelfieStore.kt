package com.oo.skinsync.data

import android.content.Context
import com.oo.skinsync.domain.SelfieStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Selfie lives in app-private filesDir only (rule #7). */
@Singleton
class AppSelfieStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : SelfieStore {

    private fun file() = File(context.filesDir, FILE_NAME)

    override fun selfieAbsolutePath(): String = file().absolutePath

    override fun deleteSelfie() {
        runCatching { file().takeIf { it.exists() }?.delete() }
    }

    private companion object {
        const val FILE_NAME = "selfie.jpg"
    }
}
