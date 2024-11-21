plugins {
    alias(libs.plugins.android.application)
}

android {

    namespace = "com.sonitasv.sonitasvapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sonitasv.sonitasvapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        signingConfigs {
            create("release") {
                storeFile = file("/Users/soniatoukkari/Desktop/Build2024/KeyStore")
                storePassword = "Koodaus10"
                keyAlias = "key0"
                keyPassword = "Koodaus10"
            }
        }

        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")
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
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}