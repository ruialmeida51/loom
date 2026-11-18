plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.library)
}

kotlin {
    android {
        namespace = "com.eyther.lumbridge.shared.features.diagnostics"
        compileSdk = 37
        minSdk = 30
        withHostTest {}
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.core)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:34.19.0"))
            implementation("com.google.firebase:firebase-crashlytics")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
    }
}

