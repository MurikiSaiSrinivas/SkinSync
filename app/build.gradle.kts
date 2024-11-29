    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.undercouch.download)
    }

    android {
        namespace = "com.oo.skinsync"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.oo.skinsync"
            minSdk = 26
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        kotlinOptions {
            jvmTarget = "1.8"
        }
        buildFeatures {
            viewBinding = true
        }
    }

    // import DownloadMPTasks task
    //val ASSET_DIR = "${projectDir}/src/main/assets"
    rootProject.extensions.add("ASSET_DIR", "${projectDir}/src/main/assets")
    apply(from = "download_tasks.gradle")

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.play.services.mlkit.face.detection)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        implementation(libs.androidx.camera.core)
        // CameraX Camera2 extensions
        implementation(libs.androidx.camera.camera2)
        // CameraX Lifecycle libraryz
        implementation(libs.androidx.camera.lifecycle)
        // CameraX View class
        implementation(libs.androidx.camera.view)
        // CameraX Extension class
        implementation(libs.androidx.camera.extensions)
        // WindowManager
        implementation(libs.androidx.window)

        // MediaPipe Library
        implementation(libs.tasks.vision)

        // Json Serializer
        implementation(libs.gson)

        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)

        // Retrofit
        implementation(libs.retrofit)
        implementation(libs.converter.gson)
        // OkHttp (optional, for logging)
        implementation(libs.logging.interceptor)
    //    implementation ("com.github.nieldw:colormath:1.0.1")

        implementation(libs.generativeai)

    }