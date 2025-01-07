import java.io.FileInputStream
import java.util.Properties

// File: app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kapt)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.openparty.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.openparty.app"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystoreProperties = Properties().apply {
        load(FileInputStream(rootProject.file("keystore.properties")))
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeType = "JKS"
        }
    }

    println("Keystore Path: ${keystoreProperties["storeFile"]}")
    println("Key Alias: ${keystoreProperties["keyAlias"]}")
    println("Store Password: ${keystoreProperties["storePassword"]}")
    println("Key Password: ${keystoreProperties["keyPassword"]}")


    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "MIXPANEL_TOKEN", "\"7df197ed50865141a46216bd58655c4e\"")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            buildConfigField("String", "MIXPANEL_TOKEN", "\"a01b6d95fd6d70bc72fa2d86ff4f5faf\"")
        }
    }

    sourceSets {
        getByName("debug") {
            res.srcDirs("src/debug/res")
        }
        getByName("release") {
            res.srcDirs("src/release/res")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.21"
    }

    kapt {
        correctErrorTypes = true
        arguments {
            arg("kapt.allowUnsafeAccess", "true")
        }
    }

}

dependencies {
    // Core Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.play.services.location)

    // Compose Libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Dependency Injection
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    kapt(libs.androidx.room.compiler)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Image Loading
    implementation(libs.coil.compose)

    // Paging
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime)

    // Unit Testing Libraries
    testImplementation(libs.junit)

    // Android Instrumented Testing Libraries
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging Tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.timber)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.storage.ktx)

    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    // Audio Player
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media)

    // Analytics
    implementation(libs.mixpanel.android)

    // Async
    implementation(libs.kotlinx.coroutines.guava)

    // Security
    implementation(libs.androidx.security.crypto)

}
