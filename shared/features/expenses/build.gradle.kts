plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.eyther.lumbridge.shared.features.expenses"
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
            implementation(projects.shared.features.analytics)
            api(projects.shared.core)
            api(projects.shared.features.household)
            implementation(projects.shared.features.account)
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
            api(libs.kotlinx.io.bytestring)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.mock)
            implementation(libs.coroutines.test)
        }
    }
}

compose.resources {
    packageOfResClass = "com.eyther.lumbridge.features.expenses.generated.resources"
}
