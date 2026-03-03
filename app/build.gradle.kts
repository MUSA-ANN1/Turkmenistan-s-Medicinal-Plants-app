import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.medicine.kitaphana"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.medicine.kitaphana"
        minSdk = 24
        targetSdk = 35
        versionCode = 7
        versionName = "3.5.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProps = Properties()
        localProps.load(FileInputStream(rootProject.file("local.properties")))

        buildConfigField("String", "QAMAR_API_KEY", "\"${localProps["QAMAR_API_KEY"]}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProps["GEMINI_API_KEY"]}\"")
        buildConfigField("String", "GEMINI_API_KEY_BACK_UP", "\"${localProps["GEMINI_API_KEY_BACK_UP"]}\"")
    }

    buildFeatures {
        buildConfig = true  // ← add this if not already there
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
    implementation(libs.material)

    implementation(libs.generativeai)
    implementation(libs.guava)

    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    implementation("androidx.work:work-runtime:2.9.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.v182)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}