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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.v182)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}