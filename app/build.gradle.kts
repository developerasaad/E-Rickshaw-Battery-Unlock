// AGP 9.2.1 + Kotlin 2.2.10 (built-in) + Compose Compiler Plugin + KSP 2.2.10-2.0.2
//
// Key AGP 9 changes applied here:
//  1. kotlin.android NOT applied — AGP 9 provides built-in Kotlin support.
//  2. kotlin.plugin.compose IS applied — Compose compiler plugin is separate.
//  3. kotlinOptions {} REMOVED — use top-level kotlin { compilerOptions { } } instead.
//  4. kapt REMOVED — all annotation processors use KSP (Room, Hilt).
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    // NO kotlin.android — AGP 9 includes built-in Kotlin support
    alias(libs.plugins.kotlin.compose)   // Compose Compiler Plugin (org.jetbrains.kotlin.plugin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.e_rickshawbatteryunlock.developerasaad"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.e_rickshawbatteryunlock.developerasaad"
        minSdk = 23
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Note: kotlinOptions {} is removed in AGP 9 — use top-level kotlin { } block below.

    buildFeatures {
        compose = true
    }

    // Exclude duplicate META-INF license files brought in by JUnit Jupiter via MockK.
    // Without this, mergeDebugAndroidTestJavaResource fails because multiple JARs
    // (junit-jupiter-api, junit-jupiter-engine, junit-platform-commons, etc.) each
    // contain META-INF/LICENSE.md with the same path.
    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/NOTICE.md"
            // Also covers any other JUnit/Kotlin duplicates that may surface
            excludes += "META-INF/*.kotlin_module"
        }
    }

}

// AGP 9 + Kotlin 2.2+ compiler options (replaces android { kotlinOptions { } })
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    // ─── Core ──────────────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // ─── Compose (version managed by BOM) ─────────────────────────────────────
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.animation)

    // ─── Navigation ────────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ─── Hilt DI ───────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)                  // KSP — not kapt
    implementation(libs.hilt.navigation.compose)

    // ─── Coroutines ────────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // ─── Room ──────────────────────────────────────────────────────────────────
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)         // KSP — not kapt

    // ─── DataStore ─────────────────────────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ─── Security ──────────────────────────────────────────────────────────────
    implementation(libs.androidx.security.crypto)

    // ─── Runtime Permissions ───────────────────────────────────────────────────
    // Note: accompanist-permissions is deprecated (feature-complete, no future updates).
    // ScanScreen uses rememberLauncherForActivityResult (official Activity Result API) instead.

    // ─── Debug ─────────────────────────────────────────────────────────────────
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // ─── Unit Tests ────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)

    // ─── Instrumented Tests ────────────────────────────────────────────────────
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
}