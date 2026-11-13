plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.eyther.lumbridge.shared.features.feed"
        compileSdk = 37
        minSdk = 30
        withHostTest {}
        androidResources { enable = true }
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.core)
            api(projects.shared.features.household)
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            api(libs.compose.material3)
            api(libs.compose.resources)
            api(libs.lifecycle.viewmodel)
            api(libs.lifecycle.runtime)
            api(libs.coroutines.core)
            api(libs.serialization.json)
            api(libs.datetime)
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
    }
}

compose.resources { packageOfResClass = "com.eyther.lumbridge.features.feed.generated.resources" }
