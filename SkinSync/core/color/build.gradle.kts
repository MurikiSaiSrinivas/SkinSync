// :core:color — PURE KOTLIN. Perceptual color math, no android.* (rule #4).
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Match Kotlin's JVM target to Java (17); otherwise it defaults to the
// running JDK (21) and Gradle rejects the inconsistency.
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(libs.junit)
}
