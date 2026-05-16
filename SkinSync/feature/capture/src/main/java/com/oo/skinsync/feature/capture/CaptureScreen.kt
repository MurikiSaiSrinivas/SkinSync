package com.oo.skinsync.feature.capture

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oo.skinsync.designsystem.Spacing
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CaptureScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) viewModel.onConsentGranted() else viewModel.onConsentDeclined() }

    LaunchedEffect(state) {
        if (state is CaptureState.Saved) onSaved()
    }

    when (val s = state) {
        is CaptureState.NeedsConsent -> ConsentDialog(
            onAgree = {
                val hasPerm = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA,
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (hasPerm) viewModel.onConsentGranted()
                else permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onDismiss = onCancel,
        )

        is CaptureState.Ready -> CameraContent(
            selfiePath = viewModel.selfieFilePath(),
            onCapture = { bmp -> viewModel.onPhotoCaptured(bmp) },
        )

        is CaptureState.Analyzing -> Centered { CircularProgressIndicator() }

        is CaptureState.Saved -> Centered { Text("Colors saved!") }

        is CaptureState.Error -> Centered {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(s.message, style = MaterialTheme.typography.bodyLarge)
                Button(onClick = viewModel::retry, modifier = Modifier.padding(top = Spacing.md)) {
                    Text("Try again")
                }
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun ConsentDialog(onAgree: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan your colors") },
        text = {
            Text(
                "SkinSync uses your camera once to read your skin, lip and eye " +
                    "colors. The photo stays only on this device and is never " +
                    "uploaded. You can delete it anytime from your profile.",
            )
        },
        confirmButton = { Button(onClick = onAgree) { Text("Allow camera") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Not now") } },
    )
}

@Composable
private fun CameraContent(
    selfiePath: String,
    onCapture: (android.graphics.Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageCapture,
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
        )
        Text(
            "For accurate colors: face soft, even daylight. Avoid colored " +
                "lighting, heavy shadows, and makeup filters.",
            style = MaterialTheme.typography.bodySmall,
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(Spacing.md)
                .background(androidx.compose.ui.graphics.Color(0x99000000))
                .padding(Spacing.sm),
        )
        Button(
            onClick = { takePhoto(context, selfiePath, imageCapture, executor, onCapture) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.xl),
        ) { Text("Capture") }
    }
}

/** Saves to app-private files dir only (rule #7 — never the public gallery). */
private fun takePhoto(
    context: Context,
    selfiePath: String,
    imageCapture: ImageCapture,
    executor: java.util.concurrent.Executor,
    onCapture: (android.graphics.Bitmap) -> Unit,
) {
    val file = File(selfiePath)
    val options = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        options,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                ContextCompat.getMainExecutor(context).execute { onCapture(bmp) }
            }

            override fun onError(exception: ImageCaptureException) {
                // Surface via state by sending a 1x1 bitmap is wrong; instead the
                // ViewModel error path is triggered by extractor failure. Log only.
                exception.printStackTrace()
            }
        },
    )
}
