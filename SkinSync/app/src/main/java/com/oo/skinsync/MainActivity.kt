package com.oo.skinsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.oo.skinsync.designsystem.SkinSyncTheme
import com.oo.skinsync.navigation.SkinSyncNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkinSyncTheme {
                SkinSyncNavHost()
            }
        }
    }
}
