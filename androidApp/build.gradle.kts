plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")

val releaseVersionCode = providers.gradleProperty("loomVersionCode").orElse("1").get().toInt()
val releaseVersionName = providers.gradleProperty("loomVersionName").get()
require(releaseVersionCode in 1..2_100_000_000) { "loomVersionCode must be a valid positive Play version code" }
require(Regex("[0-9][A-Za-z0-9.+_-]{0,49}").matches(releaseVersionName)) { "Invalid loomVersionName" }

val uploadStorePath = providers.environmentVariable("LOOM_UPLOAD_STORE_FILE").orNull

android {
    namespace = "com.eyther.lumbridge"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.eyther.lumbridge"
        minSdk = 30
        targetSdk = 37
        versionCode = releaseVersionCode
        versionName = releaseVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        if (uploadStorePath != null) {
            create("upload") {
                storeFile = file(uploadStorePath)
                storePassword = providers.environmentVariable("LOOM_UPLOAD_STORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("LOOM_UPLOAD_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("LOOM_UPLOAD_KEY_PASSWORD").get()
            }
        }
    }
    buildTypes {
        release {
            if (uploadStorePath != null) signingConfig = signingConfigs.getByName("upload")
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
    constraints {
        implementation("androidx.concurrent:concurrent-futures:1.2.0") {
            because("The application and AndroidX Test must resolve the same runtime version")
        }
    }
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.core:core-ktx:1.19.1")
    implementation(projects.shared.entrypoint)
    implementation(projects.shared.core)
    implementation(projects.shared.features.settings)
    implementation(libs.activity.compose)
    androidTestImplementation("org.jetbrains.compose.ui:ui-test-junit4:1.12.1")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}
