plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "com.eyther.lumbridge.shared.di"
        compileSdk = 37
        minSdk = 30
        withHostTest {}
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core)
            implementation(projects.shared.features.analytics)
            implementation(projects.shared.features.authentication)
            implementation(projects.shared.features.expenses)
            implementation(projects.shared.features.feed)
            implementation(projects.shared.features.home)
            implementation(projects.shared.features.household)
            implementation(projects.shared.features.insights)
            implementation(projects.shared.features.legal)
            implementation(projects.shared.features.reminders)
            implementation(projects.shared.features.settings)
            implementation(projects.shared.features.shopping)
            api(libs.koin.core)
            implementation(libs.koin.annotations)
            implementation(libs.koin.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
    }
}
