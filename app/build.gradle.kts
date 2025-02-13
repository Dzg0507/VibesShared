plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt) // Correct: Use alias
    alias(libs.plugins.hilt)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.compose.compiler)


}

android {
    namespace = "com.example.vibesshared" // Replace
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.vibesshared" // Replace
        minSdk = 26
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
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    buildToolsVersion = "36.0.0 rc4"
    ndkVersion = "28.0.13004108"
}

//  ****  Put kapt configuration *before* dependencies ****
kapt {
    correctErrorTypes = true
}

dependencies {
    //Compose
    implementation(libs.androidx.compose.bom) // Use the latest stable version

    // Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.material.icons.extended)
    implementation("androidx.compose.material:material-icons-extended:1.7.7")


    //Core
    implementation(libs.jetbrains.kotlin.stdlib)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)





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
    kapt(libs.hilt.compiler) // Correct kapt usage
    implementation(libs.hilt.navigation.compose)

    //Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    //Image Loading
    implementation(libs.glide)
    kapt(libs.glide.compiler) // Correct kapt usage
    implementation(libs.coil.compose)



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
    implementation(libs.androidx.graphics.core)
    implementation ("com.airbnb.android:lottie-compose:6.6.2")

}