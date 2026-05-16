plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // ── Enable these two lines AFTER adding google-services.json to app/ ──
    // (Firebase SDKs below compile without it; runtime/AI/Firestore need it.)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.oo.skinsync"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.oo.skinsync"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:color"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ml"))
    implementation(project(":data"))
    implementation(project(":feature:capture"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:result"))
    implementation(project(":feature:wardrobe"))
    implementation(project(":feature:history"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)

    // Firebase SDKs (compile without google-services.json; runtime needs it).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.appcheck.playintegrity)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
