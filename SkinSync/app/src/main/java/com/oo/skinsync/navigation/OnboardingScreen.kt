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
import com.oo.skinsync.designsystem.Spacing

/** First-run intro + privacy explainer. */
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    Scaffold { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Welcome to SkinSync", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Scan your skin, lip and eye colors, then get outfit colors that " +
                    "flatter you for any photoshoot location.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Privacy: your selfie stays on this device and is never uploaded. " +
                    "Only anonymous, non-identifying data is used. You can delete " +
                    "everything anytime from your profile.",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = onGetStarted) { Text("Get started") }
        }
    }
}
