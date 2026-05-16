// :core:color — PURE KOTLIN. Perceptual color math, no android.* (rule #4).
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation(libs.junit)
}
