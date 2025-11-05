import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    FileInputStream(localPropertiesFile).use { stream ->
        localProperties.load(stream)
    }
}

android {
    namespace = "com.qingshuige.tangyuan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.qingshuige.tangyuan"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // OpenPanel Analytics Configuration
        // Priority: Environment Variables > local.properties > Default Values
        val openPanelClientId = System.getenv("OPENPANEL_CLIENT_ID")
            ?: localProperties.getProperty("openpanel.client.id")
            ?: ""
        val openPanelClientSecret = System.getenv("OPENPANEL_CLIENT_SECRET")
            ?: localProperties.getProperty("openpanel.client.secret")
            ?: ""
        val openPanelBaseUrl = System.getenv("OPENPANEL_BASE_URL")
            ?: localProperties.getProperty("openpanel.base.url")
            ?: "https://openpanel.grtsinry43.com/api/"

        buildConfigField("String", "OPENPANEL_CLIENT_ID", "\"$openPanelClientId\"")
        buildConfigField("String", "OPENPANEL_CLIENT_SECRET", "\"$openPanelClientSecret\"")
        buildConfigField("String", "OPENPANEL_BASE_URL", "\"$openPanelBaseUrl\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.androidx.navigation.compose)
    
    // Network dependencies
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.datastore.preferences)
    
    // JWT library
    implementation(libs.java.jwt)
    
    // Hilt dependencies
    implementation(libs.hilt.android)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.foundation)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // ViewModel and Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.pangu.jvm)

    // QR Code generation
    implementation(libs.core)

    // Haze for blur effect
    implementation(libs.haze.materials)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}