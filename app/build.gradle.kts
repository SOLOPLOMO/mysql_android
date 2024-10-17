import org.gradle.internal.declarativedsl.parsing.main


plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "gr.app.JNITHESIS"
    compileSdk = 35
    // Link Gradle
    externalNativeBuild{
        cmake{
            path("cpp\\CMakeLists.txt")
        }
    }



    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    defaultConfig {
        applicationId = "gr.app.jni_android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake{
                cppFlags("")
                arguments("-DANDROID_STL=c++_shared")
            }
        }
        ndk{
            abiFilters.add("armeabi-v7a")
            //abiFilters.add("x86") deprecated
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86_64")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.github.tapadoo:alerter:7.2.4")
}