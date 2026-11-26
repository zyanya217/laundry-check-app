import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// AGP 9 compiles Kotlin itself, so there is no org.jetbrains.kotlin.android plugin here.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.github.zyanya217.laundrycheck"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.zyanya217.laundrycheck"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        // The web app's Cloudflare Pages deployment. It holds the CWA key; the app never sees it.
        buildConfigField("String", "FORECAST_BASE_URL", "\"https://laundry-check.pages.dev/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Personal app: sign release builds with the debug key so `assembleRelease` just works.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    debugImplementation(libs.androidx.compose.ui.tooling)

    ksp(libs.hilt.compiler)
    ksp(libs.kotlin.metadata)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
