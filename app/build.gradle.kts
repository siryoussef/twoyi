plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "io.twoyi"
    
    defaultConfig {
        applicationId = "io.twoyi"
        compileSdk = 34
        minSdk = 27
        targetSdk = 27
        versionCode = 1
        versionName = "0.1"

        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.clans:fab:1.6.4")
    implementation("com.afollestad.material-dialogs:core:0.9.6.0")
    implementation("com.github.bumptech.glide:glide:5.0.0-rc01")
    kapt("com.github.bumptech.glide:compiler:5.0.0-rc01")

    implementation("org.apache.commons:commons-compress:1.26.2")
    implementation("com.github.hzy3774:AndroidP7zip:1.7.2")

    implementation("com.github.tiann:FreeReflection:3.2.2")
    implementation("com.github.topjohnwu.libsu:core:5.2.2")
    implementation("org.jdeferred:jdeferred-android-aar:1.2.6")
    kapt("androidx.annotation:annotation:1.8.2")
}