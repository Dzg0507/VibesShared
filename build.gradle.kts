// Top-level build file where you can add configuration options common to all sub-projects/modules.



plugins {

    id("com.android.library") version "8.1.4" apply false
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.autonomousapps.dependency-analysis") version "2.6.1" apply false
    id("com.github.ben-manes.versions") version "0.51.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle) // Or latest
        classpath(libs.google.services)
    }
}