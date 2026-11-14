plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.eyther.lumbridge.shared.entrypoint"
        compileSdk = 37
        minSdk = 30
        withHostTest {}
        androidResources { enable = true }
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "LoomFoundation"
            isStatic = true
            binaryOption("bundleId", "com.eyther.lumbridge.foundation")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.di)
            implementation(projects.shared.features.legal)
            implementation(projects.shared.features.analytics)
            api(projects.shared.core)
            implementation(projects.shared.features.authentication)
            implementation(projects.shared.features.household)
            implementation(projects.shared.features.shopping)
            implementation(projects.shared.features.reminders)
            implementation(projects.shared.features.expenses)
            implementation(projects.shared.features.feed)
            implementation(projects.shared.features.insights)
            implementation(projects.shared.features.settings)
            implementation(projects.shared.features.home)
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

compose.resources {
    packageOfResClass = "com.eyther.lumbridge.shared.entrypoint.generated.resources"
}

kotlin {
    sourceSets {
        val jvmTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.compose.ui:ui-test-junit4:1.12.1")
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    systemProperty("java.awt.headless", "true")
    systemProperty("skiko.data.path", layout.buildDirectory.dir("skiko").get().asFile.absolutePath)
    systemProperty(
        "loom.screenshots.dir",
        rootProject.layout.buildDirectory.dir("screenshots").get().asFile.absolutePath,
    )
}
