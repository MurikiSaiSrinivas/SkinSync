package com.oo.skinsync.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oo.skinsync.domain.FaceColors
import com.oo.skinsync.domain.Gender
import com.oo.skinsync.domain.Profile
import com.oo.skinsync.domain.UiState
import com.oo.skinsync.designsystem.Spacing

@Composable
fun ProfileScreen(
    onScanColors: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is UiState.Empty -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No profile yet.") }
        is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message) }
        is UiState.Success -> ProfileForm(
            profile = s.data,
            onName = viewModel::onNameChange,
            onAge = viewModel::onAgeChange,
            onGender = viewModel::onGenderChange,
            onSave = viewModel::save,
            onDelete = viewModel::deleteMyData,
            onScanColors = onScanColors,
        )
    }
}

@Composable
private fun ProfileForm(
    profile: Profile,
    onName: (String) -> Unit,
    onAge: (String) -> Unit,
    onGender: (Gender) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onScanColors: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text("Your profile", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = profile.name,
            onValueChange = onName,
            label = { Text("Name") },
            singleLine = true,
        )
        OutlinedTextField(
            value = profile.age?.toString().orEmpty(),
            onValueChange = onAge,
            label = { Text("Age") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Gender.entries.forEach { g ->
                FilterChip(
                    selected = profile.gender == g,
                    onClick = { onGender(g) },
                    label = { Text(g.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }

        Text("Your colors", style = MaterialTheme.typography.titleMedium)
        if (profile.faceColors == null) {
            Text("Not scanned yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            val fc = profile.faceColors!!
            Swatches(fc)
            val season = remember(fc) {
                com.oo.skinsync.color.SeasonalAnalyzer.analyze(fc.skin, fc.lip, fc.leftEye)
            }
            Text(
                "Your season: $season",
                style = MaterialTheme.typography.titleSmall,
            )
        }
        OutlinedButton(onClick = onScanColors) { Text("Scan / rescan colors") }

        Button(onClick = onSave) { Text("Save") }
        OutlinedButton(onClick = { confirmDelete = true }) { Text("Delete my data") }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete my data?") },
            text = { Text("This erases your profile and the on-device selfie. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Swatches(colors: FaceColors) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        listOf(
            "Skin" to colors.skin,
            "Lip" to colors.lip,
            "L eye" to colors.leftEye,
            "R eye" to colors.rightEye,
        ).forEach { (label, argb) ->
            val hex = "#%06X".format(0xFFFFFF and argb)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(Color(argb), CircleShape)
                        .semantics { contentDescription = "$label color $hex" },
                )
                Text(label, style = MaterialTheme.typography.labelSmall)
                // B8: explicit value text so it's readable without color vision.
                Text(hex, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
