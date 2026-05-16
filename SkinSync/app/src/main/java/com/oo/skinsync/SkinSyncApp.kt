package com.oo.skinsync

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SkinSyncApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    /**
     * Guarded: until the user adds google-services.json + enables the
     * google-services plugin, FirebaseApp won't initialize — the app must
     * still run (non-Firebase features work). Rule #2: no keys in the app.
     */
    private fun initFirebase() {
        runCatching {
            if (FirebaseApp.getApps(this).isEmpty()) return
            Firebase.appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance(),
            )
            if (Firebase.auth.currentUser == null) {
                Firebase.auth.signInAnonymously()
            }
        }.onFailure { Log.w("SkinSyncApp", "Firebase not configured yet: ${it.message}") }
    }
}
