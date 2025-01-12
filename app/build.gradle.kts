


plugins {
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.8.21"
    id("com.google.gms.google-services")
    id("com.github.fkorotkov.libraries") version "1.1"



}

android {
    namespace = "com.example.vibesshared"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.vibesshared"
        minSdk = 28
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "2.1.0" // Replace with your compose version
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_20
            targetCompatibility = JavaVersion.VERSION_20
        }
        kotlinOptions {
            jvmTarget = "20"
        }
        buildFeatures {
            compose = true
        }
        composeOptions {
            kotlinCompilerExtensionVersion = "2.0.0" // Replace with your compose version(2.0) // Replace with your compose version
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }


        dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.activity.compose)

            // Use the Compose BOM to manage versions

            implementation(platform(libs.androidx.compose.bom.v20241201))
            implementation(libs.androidx.compose.material3) // Include Material 3

            // For navigation
            implementation(libs.androidx.navigation.compose)


            // Material icons (core and extended)
            implementation(libs.androidx.material.icons.core)
            implementation(libs.material.icons.extended)
            implementation(libs.androidx.material3.android)
            implementation(libs.androidx.foundation.android)
            implementation(libs.androidx.ui.tooling)
            implementation(libs.androidx.espresso.contrib)
            implementation(libs.androidx.animation.core.android)
            implementation(libs.androidx.animation.android)
            implementation(libs.firebase.crashlytics.buildtools)
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.kotlin.stdlib)
            implementation(libs.androidx.foundation.android)


            // Your test dependencies
            testImplementation(libs.junit)
            androidTestImplementation(libs.androidx.junit)
            androidTestImplementation(libs.androidx.espresso.core)
            androidTestImplementation(platform(libs.androidx.compose.bom.v20241201))
            androidTestImplementation(libs.ui.test.junit4)
            debugImplementation(libs.ui.tooling)
            debugImplementation(libs.ui.test.manifest)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.glide) // Or latest version
            annotationProcessor(libs.glide.compiler)
            implementation(libs.androidx.media3.exoplayer) // Or latest version
            implementation(libs.androidx.media3.ui)
            implementation(libs.firebase.database.ktx)
            implementation(libs.slf4j.api)
            implementation(libs.slf4j.simple)
            implementation(libs.androidx.compose.material3)
            implementation(libs.coil.compose)
            implementation(libs.kotlinx.coroutines.play.services)
            implementation(libs.lottie)
            implementation(libs.lottie.compose)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.kotlin.reflect)
            implementation(libs.coil.compose) // Or latest version
            implementation(platform(libs.firebase.bom))
            implementation(libs.firebase.auth.ktx)// Or latest version
            implementation(libs.coil.network.okhttp)
            implementation(libs.coil.kt.coil.compose)



        }
    }
}
dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.animation.core.android)
}
