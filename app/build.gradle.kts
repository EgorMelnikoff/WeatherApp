plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtoolsKsp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.egormelnikoff.myweather"
    compileSdk = 36
    viewBinding {
        enable = true
    }
    defaultConfig {
        applicationId = "com.egormelnikoff.myweather"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1-alpha"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation (libs.androidx.datastore.preferences)
    implementation (libs.gson)
    implementation (libs.androidx.swiperefreshlayout)
    annotationProcessor(libs.androidx.room.room.compiler)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.androidx.work.runtime.ktx)
    implementation (libs.play.services.location)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    ksp(libs.androidx.room.room.compiler)
}