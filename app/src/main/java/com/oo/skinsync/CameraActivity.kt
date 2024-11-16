package com.oo.skinsync

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.oo.skinsync.databinding.ActivityCameraBinding
import com.oo.skinsync.extensions.saveFaceAnalysisResult
import com.oo.skinsync.models.CameraState
import com.oo.skinsync.viewmodels.CameraViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private val viewModel: CameraViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceLandmarker: FaceLandmarker
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var progressBar: ProgressBar

    private val sharedPreferences by lazy {
        getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = findViewById(R.id.loading_spinner)
        hideLoadingSpinner()

        setupObservers()
        setupClickListeners()
        initializeCamera()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.cameraState.collect { state ->
                when (state) {
                    is CameraState.Preview -> Unit // Handle preview state
                    is CameraState.ImageCapture -> takePhoto()
                    is CameraState.Error -> handleError(state.message)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.faceAnalysisResult.collect { result ->
                result?.let { faceResult ->
                    sharedPreferences.saveFaceAnalysisResult(faceResult)
                }
            }
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun setupClickListeners() {
        binding.cameraFlipButton.setOnClickListener { flipCamera() }
        binding.imageCaptureButton.setOnClickListener {
            viewModel.setCameraState(CameraState.ImageCapture)
        }
    }

    private fun initializeCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        initializeFaceLandmarker()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun initializeFaceLandmarker() {
        // Configure the face landmark detector options
        val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath("assets/face_landmarker.task")
//        val baseOptions = baseOptionsBuilder.build()

        val optionsBuilder =
            FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinFaceDetectionConfidence(minFaceDetectionConfidence)
                .setMinTrackingConfidence(minFaceTrackingConfidence)
                .setMinFacePresenceConfidence(minFacePresenceConfidence)
                .setNumFaces(maxNumFaces)
                .setRunningMode(RunningMode.IMAGE)

        val options = optionsBuilder.build()
        faceLandmarker = FaceLandmarker.createFromOptions(this, options)
    }

    private fun flipCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, generateFileName())
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        sharedPreferences.edit()
                            .putString("profile_image_uri", uri.toString())
                            .apply()
                        detectFaceLandmarks(uri)
                    }
                    navigateToMain()
                }

                override fun onError(exc: ImageCaptureException) {
                    handleError("Photo capture failed: ${exc.message}")
                }
            }
        )
    }

    private fun showLoadingSpinner() {
        Log.d(TAG, "Spinner should Visible")
        progressBar.bringToFront()
        binding.dimOverlay.bringToFront()
        progressBar.visibility = View.VISIBLE
        binding.dimOverlay.visibility = View.VISIBLE
    }

    private fun hideLoadingSpinner() {
        Log.d(TAG, "Spinner should Hide")
        progressBar.visibility = View.GONE
//        binding.loadingSpinner.visibility = View.GONE
        binding.dimOverlay.visibility = View.GONE
    }

    private fun detectFaceLandmarks(imageUri: Uri) {

        showLoadingSpinner()
        // Convert URI to Bitmap
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

        // Convert Bitmap to MediaPipe Image
        val mpImage = BitmapImageBuilder(bitmap).build()


        // Run the face landmark detection
        val result: FaceLandmarkerResult = faceLandmarker.detect(mpImage)

        Log.d("Landmarks", result.faceLandmarks().toString())

        if (result.faceLandmarks().isNotEmpty()) {
            val landmarks = result.faceLandmarks()[0]
            viewModel.processFaceLandmarks(landmarks, bitmap)
            hideLoadingSpinner()
        } else {
            Log.d("FaceLandmarks", "No face detected")
            hideLoadingSpinner()
        }
    }

    private fun handleError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message)
    }

    private fun generateFileName(): String {
        return SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "com.oo.skinsync.CameraActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private var minFaceDetectionConfidence: Float = 0.5F
        private var minFaceTrackingConfidence: Float = 0.5F
        private var minFacePresenceConfidence: Float = 0.5F
        private var maxNumFaces: Int = 1
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}