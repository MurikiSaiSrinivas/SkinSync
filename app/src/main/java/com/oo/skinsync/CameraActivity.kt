package com.oo.skinsync

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mlkit.vision.face.FaceLandmark
import com.oo.skinsync.databinding.ActivityCameraBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.pow


class CameraActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCameraBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var faceLandmarker: FaceLandmarker

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var cameraProvider: ProcessCameraProvider

    private var minFaceDetectionConfidence: Float = 0.5F
    private var minFaceTrackingConfidence: Float = 0.5F
    private var minFacePresenceConfidence: Float = 0.5F
    private var maxNumFaces: Int = 1

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        viewBinding.cameraFlipButton.setOnClickListener{ flipCamera() }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()
        initializeFaceLandmarker()

    }

    private fun flipCamera(){
        // Toggle between front and back cameras
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        cameraProvider.unbindAll()
        // Restart the camera with the new selector
        startCamera()
    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    output.savedUri?.let { uri ->
                        detectFaceLandmarks(uri)
                    }
                }
            }
        )
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
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

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

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private fun initializeFaceLandmarker() {
        // Configure the face landmark detector options
        val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath("assets/face_landmarker.task")
        val baseOptions = baseOptionsBuilder.build()

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

    private fun detectFaceLandmarks(imageUri: Uri) {
        // Convert URI to Bitmap
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

        // Convert Bitmap to MediaPipe Image
        val mpImage = BitmapImageBuilder(bitmap).build()


        // Run the face landmark detection
        val result: FaceLandmarkerResult = faceLandmarker.detect(mpImage)

        if (result.faceLandmarks().isNotEmpty()) {
            val landmarks = result.faceLandmarks()[0] // First detected face

            // Extract specific regions
            val cheekPixels = extractRegionPixels(bitmap, landmarks, listOf(1, 6, 197, 195, 49, 98, 278, 327, 50, 101, 118, 280, 330, 347))
            val lipPixels = extractRegionPixels(bitmap, landmarks, listOf(13, 14, 78, 81, 82, 17, 84, 91, 61, 291))
            val leftEyePixels = extractRegionPixels(bitmap, landmarks, listOf(133, 173, 157, 158))
            val rightEyePixels = extractRegionPixels(bitmap, landmarks, listOf(362, 398, 384, 385))

            // Analyze the dominant color of each region
            val cheekColor = findDominantColor(cheekPixels)
            val lipColor = findDominantColor(lipPixels)
            val leftEyeColor = findDominantColor(leftEyePixels)
            val rightEyeColor = findDominantColor(rightEyePixels)

            Log.d("SkinSync", "Cheek Color: ${Integer.toHexString(cheekColor)}")
            Log.d("SkinSync", "Lip Color: ${Integer.toHexString(lipColor)}")
            Log.d("SkinSync", "Left Eye Color: ${Integer.toHexString(leftEyeColor)}")
            Log.d("SkinSync", "Right Eye Color: ${Integer.toHexString(rightEyeColor)}")

            val markedBitmap = drawLandmarksOnBitmap(bitmap, landmarks)
            saveBitmapWithLandmarks(markedBitmap, imageUri)


        } else {
            Log.d("FaceLandmarks", "No face detected")
        }
    }

    private fun extractRegionPixels(
        bitmap: Bitmap,
        landmarks: MutableList<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>,
        indices: List<Int>
    ): List<FloatArray> {
        val regionPixels = mutableListOf<FloatArray>()
        for (index in indices) {
            val landmark = landmarks[index]
            val x = (landmark.x() * bitmap.width).toInt()
            val y = (landmark.y() * bitmap.height).toInt()
            if (x in 0 until bitmap.width && y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel).toFloat()
                val g = Color.green(pixel).toFloat()
                val b = Color.blue(pixel).toFloat()
                regionPixels.add(floatArrayOf(r, g, b))
            }
        }
        return regionPixels
    }

    private fun findDominantColor(pixels: List<FloatArray>, k: Int = 3): Int {
        val centroids = pixels.shuffled().take(k).toMutableList()

        repeat(10) {  // Iterate to improve clustering accuracy
            val clusters = centroids.map { mutableListOf<FloatArray>() }
            for (pixel in pixels) {
                val closestCentroid = centroids.minByOrNull { centroid ->
                    (centroid[0] - pixel[0]).pow(2) + (centroid[1] - pixel[1]).pow(2) +
                            (centroid[2] - pixel[2]).pow(2)  // Euclidean distance
                }
                clusters[centroids.indexOf(closestCentroid)].add(pixel)
            }
            for (i in centroids.indices) {
                val cluster = clusters[i]
                if (cluster.isNotEmpty()) {
                    val r = cluster.sumOf { it[0].toDouble() } / cluster.size
                    val g = cluster.sumOf { it[1].toDouble() } / cluster.size
                    val b = cluster.sumOf { it[2].toDouble() } / cluster.size
                    centroids[i] = floatArrayOf(r.toFloat(), g.toFloat(), b.toFloat())
                }
            }
        }
        val dominant = centroids.maxByOrNull { centroid ->
            pixels.count { it.contentEquals(centroid) }
        } ?: centroids.first()

        return Color.rgb(dominant[0].toInt(), dominant[1].toInt(), dominant[2].toInt())
    }

//    private fun drawLandmarksOnBitmap(bitmap: Bitmap, landmarks: MutableList<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>): Bitmap {
//        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//        val canvas = Canvas(mutableBitmap)
//        val paint = Paint().apply {
//            color = Color.RED
//            style = Paint.Style.FILL
//            strokeWidth = 4f
//        }
//
//        landmarks.forEach { landmark ->
//            canvas.drawCircle(landmark.x() * bitmap.width, landmark.y() * bitmap.height, 8f, paint)
//        }
//        return mutableBitmap
//    }

    private fun drawLandmarksOnBitmap(bitmap: Bitmap, landmarks: MutableList<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>): Bitmap {
        // Create a mutable copy of the original bitmap to draw on
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.RED // Color of the text
            textSize = 20f // Adjust size as needed
            style = Paint.Style.FILL
        }

        // Draw the index of each landmark on the canvas
        for (i in 0 until landmarks.count()) {
            val landmark = landmarks.get(i)
            val x = landmark.x() * bitmap.width
            val y = landmark.y() * bitmap.height

            // Draw the index at the landmark's position
            canvas.drawText(i.toString(), x, y, paint)
        }

        return mutableBitmap
    }


    private fun saveBitmapWithLandmarks(bitmap: Bitmap, originalUri: Uri) {
        try {
            // Open an output stream to save the modified bitmap
            val outputStream = contentResolver.openOutputStream(originalUri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            outputStream?.close()
            Log.d(TAG, "Bitmap with landmarks saved successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap with landmarks: ${e.message}", e)
        }
    }

//    private fun saveBitmapWithLandmarks(bitmap: Bitmap) {
//        // Create time stamped name and MediaStore entry for saving the modified image
//        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
//            }
//        }
//
//        // Create output options object which contains file + metadata
//        val outputOptions = ImageCapture.OutputFileOptions
//            .Builder(contentResolver,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                contentValues)
//            .build()
//
//        // Save the bitmap using an OutputStream
//        try {
//            val outputStream = contentResolver.openOutputStream(outputOptions.savedUri!!)
//            if (outputStream != null) {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            }
//            outputStream?.close()
//            Log.d(TAG, "Bitmap with landmarks saved successfully.")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error saving bitmap with landmarks: ${e.message}", e)
//        }
//    }
}
