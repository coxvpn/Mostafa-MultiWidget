plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "app.myipwidget"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.myipwidget"
        minSdk = 26
        targetSdk = 34
        versionCode = 61
        versionName = "1.19.1"
    }

    // A fixed key, so each new version installs over the previous one
    // (the build machine would otherwise invent a new key every time).
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
