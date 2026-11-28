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
        versionCode = 93
        versionName = "1.21.27"
    }

    // The private key: the build machine gets it from the repository's Secrets (see README), it is not in the code.
    // The same key for every version, so each new version installs over the previous one.
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_FILE")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    lint {
        checkReleaseBuilds = false   // a lint warning must not stop a release
        abortOnError = false
    }
    // the file is named after the app and its version (no "app-debug")
    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Mostafa-MultiWidget-v${versionName}.apk"
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
