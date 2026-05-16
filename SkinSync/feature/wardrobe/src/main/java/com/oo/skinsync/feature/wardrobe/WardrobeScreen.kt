package com.oo.skinsync.feature.wardrobe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oo.skinsync.designsystem.Spacing
import java.io.File
import java.util.concurrent.Executors

@Composable
fun WardrobeScreen(
    onCancel: () -> Unit,
    viewModel: WardrobeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var linkColor by remember { mutableStateOf<Int?>(null) }

    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) viewModel.onConsentGranted() else viewModel.onConsentDeclined() }

    when (val s = state) {
        is WardrobeState.NeedsConsent -> AlertDialog(
            onDismissRequest = onCancel,
            title = { Text("Match an outfit") },
            text = { Text("Photograph a clothing item; SkinSync finds colors that pair with it. The photo stays on this device.") },
            confirmButton = {
                Button(onClick = {
                    val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                    if (ok) viewModel.onConsentGranted() else permission.launch(Manifest.permission.CAMERA)
                }) { Text("Allow camera") }
            },
            dismissButton = { TextButton(onClick = onCancel) { Text("Not now") } },
        )

        is WardrobeState.Ready -> Camera { viewModel.onPhotoCaptured(it) }

        is WardrobeState.Analyzing -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

        is WardrobeState.Result -> Column(
            Modifier.fillMaxSize().padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text("Your ${s.colorName} item", style = MaterialTheme.typography.titleMedium)
            Box(Modifier.size(64.dp).background(Color(s.baseColor), CircleShape))
            Text("Pairs well with:", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                s.matches.forEach { c ->
                    Box(
                        Modifier.size(48.dp).background(Color(c), CircleShape),
                    ) { TextButton(onClick = { linkColor = c }) { Text(" ") } }
                }
            }
            Text("Tap a color to shop it.", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = viewModel::retry) { Text("Scan another") }
        }

        is WardrobeState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(s.message)
                Button(onClick = viewModel::retry, modifier = Modifier.padding(top = Spacing.md)) { Text("Try again") }
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }

    linkColor?.let { c ->
        val links = viewModel.linksFor(c)
        AlertDialog(
            onDismissRequest = { linkColor = null },
            title = { Text("Shop this color") },
            text = {
                Column {
                    links.forEach { link ->
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                            linkColor = null
                        }) { Text(link.retailer) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { linkColor = null }) { Text("Close") } },
        )
    }
}

@Composable
private fun Camera(onCapture: (android.graphics.Bitmap) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val view = PreviewView(ctx)
                val future = ProcessCameraProvider.getInstance(ctx)
                future.addListener({
                    val provider = future.get()
                    val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                }, ContextCompat.getMainExecutor(ctx))
                view
            },
        )
        Button(
            onClick = {
                val file = File(context.cacheDir, "garment.jpg")
                imageCapture.takePicture(
                    ImageCapture.OutputFileOptions.Builder(file).build(),
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(o: ImageCapture.OutputFileResults) {
                            val bmp = BitmapFactory.decodeFile(file.absolutePath)
                            ContextCompat.getMainExecutor(context).execute { onCapture(bmp) }
                        }
                        override fun onError(e: ImageCaptureException) { e.printStackTrace() }
                    },
                )
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.xl),
        ) { Text("Capture garment") }
    }
}
