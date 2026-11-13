plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")

android {
    namespace = "com.eyther.lumbridge"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.eyther.lumbridge"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.core:core-ktx:1.19.1")
    implementation(project(":shared:entrypoint"))
    implementation(project(":shared:core"))
    implementation(libs.activity.compose)
}
