package com.oo.skinsync.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * App theme. Dark theme is fully supported (rule: the old app had an empty
 * values-night). No dynamic color so branding stays consistent.
 */
@Composable
fun SkinSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SkinSyncTypography,
        shapes = SkinSyncShapes,
        content = content,
    )
}
