plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") // <- add this
    alias(libs.plugins.hilt)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.vibesshared"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.vibesshared"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "false", // Temporarily disable incremental processing
                    "room.expandProjection" to "true"
                )
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    buildToolsVersion = "36.0.0 rc4"
    ndkVersion = "28.0.13004108"
}



dependencies {
    // Add the kotlinx-metadata-jvm dependency at the top level to ensure it's loaded first

    //Compose
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material.icons.extended)
    implementation(libs.io.coil.kt.coil.gif)


    //Core
    implementation(libs.jetbrains.kotlin.stdlib)
    implementation(libs.androidx.graphics.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    //3D Rendering
    implementation("io.github.sceneview:sceneview:2.2.1")
    implementation("com.google.android.filament:filament-android:1.57.1")
    implementation("com.google.android.filament:filament-utils-android:1.57.1")
    implementation("com.google.android.filament:gltfio-android:1.57.1")

    //Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    //Navigation
    implementation(libs.androidx.navigation.compose)

    //Lottie
    implementation(libs.lottie)

    //Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.animation.core.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    //Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    //Image Loading
    implementation(libs.glide)
    ksp(libs.glide.compiler)
    implementation(libs.coil.compose)
    implementation(libs.androidx.media3.transformer)

    //Room LocalCache - explicitly use version numbers for more control
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.storage)
    implementation(libs.com.google.firebase.firebase.auth)

    //Serialization
    implementation(libs.kotlinx.serialization.json)

    //Google Play Services
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)

    //DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)

    //Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)

    //Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)
    testImplementation(libs.mockk)
}