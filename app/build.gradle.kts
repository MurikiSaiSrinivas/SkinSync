    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.undercouch.download)
        id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    }

    secrets {
        // To add your Maps API key to this project:
        // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
        // 2. Add this line, where YOUR_API_KEY is your API key:
        //        MAPS_API_KEY=YOUR_API_KEY
        propertiesFileName = "secrets.properties"

        // A properties file containing default secret values. This file can be
        // checked in version control.
        defaultPropertiesFileName = "local.properties"

        // Configure which keys should be ignored by the plugin by providing regular expressions.
        // "sdk.dir" is ignored by default.
        ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
        ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
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

//            buildConfigField("String", "SERP_API_KEY", "\"${project.properties["SERP_API_KEY"] as String}\"")
            buildConfigField("String", "GEMINI_API_KEY", "\"${findProperty("SERP_API_KEY")}\"")
            println("SERP_API_KEY: ${findProperty("SERP_API_KEY")}")
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
            buildConfig = true
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
        implementation(libs.generativeai)
        implementation(libs.androidx.cardview)
        implementation(libs.glide)
        annotationProcessor(libs.compiler)
    }