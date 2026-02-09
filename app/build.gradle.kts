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
        versionCode = 4
        versionName = "2"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.material)

    //noinspection UseTomlInstead
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.activity:activity:1.8.2")
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}