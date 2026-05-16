package com.oo.skinsync.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.oo.skinsync.designsystem.SkinSyncTheme
import com.oo.skinsync.designsystem.Spacing

/**
 * Temporary home until the feature screens land (Phases 1–5). Proves the
 * theme, navigation, and module wiring are correct.
 */
@Composable
fun HomePlaceholder(
    onProfile: () -> Unit,
    onCapture: () -> Unit,
    onResult: () -> Unit,
    onHistory: () -> Unit,
    onWardrobe: () -> Unit,
) {
    Scaffold { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("SkinSync", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Find your photoshoot palette.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onCapture) { Text("Scan my colors") }
            Button(onClick = onResult) { Text("Get my palette") }
            Button(onClick = onHistory) { Text("Saved looks") }
            Button(onClick = onWardrobe) { Text("Match an outfit") }
            Button(onClick = onProfile) { Text("My profile") }
        }
    }
}

@Preview
@Composable
private fun HomePreview() {
    SkinSyncTheme {
        HomePlaceholder(onProfile = {}, onCapture = {}, onResult = {}, onHistory = {}, onWardrobe = {})
    }
}
