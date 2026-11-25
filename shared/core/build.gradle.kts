import java.util.Properties

plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.eyther.lumbridge.shared.core"
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
            api("org.jetbrains.androidx.navigation3:navigation3-ui:1.2.0-beta01")
            api("androidx.navigation3:navigation3-runtime:1.2.0")
            api("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0")
            implementation("com.russhwolf:multiplatform-settings:1.3.0")
            api(libs.collections.immutable)
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
            api(libs.ktor.core)
            api(libs.supabase.auth)
            api(libs.supabase.postgrest)
            api(libs.supabase.functions)
            api(libs.supabase.storage)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
            implementation(libs.ktor.mock)
        }

        androidMain.dependencies {
            implementation(libs.ktor.okhttp)
            implementation("androidx.core:core-ktx:1.19.1")
        }
        jvmMain.dependencies {
            implementation(libs.ktor.okhttp)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.11.0")
        }
        iosMain.dependencies { implementation(libs.ktor.darwin) }
    }
}

val loomProperties =
    Properties().apply {
        rootProject
            .file("config/loom-public.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use { load(it) }
        rootProject.file("loom.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
val loomUrl =
    providers.environmentVariable("SUPABASE_URL").orNull
        ?: loomProperties.getProperty("SUPABASE_URL", "")
val loomKey =
    providers.environmentVariable("SUPABASE_PUBLISHABLE_KEY").orNull
        ?: loomProperties.getProperty("SUPABASE_PUBLISHABLE_KEY", "")

require(loomUrl.isBlank() || Regex("https?://[A-Za-z0-9.:-]+/?").matches(loomUrl)) {
    "Invalid SUPABASE_URL"
}

require(loomKey.isBlank() || Regex("[A-Za-z0-9._-]+").matches(loomKey)) {
    "Invalid SUPABASE_PUBLISHABLE_KEY"
}

require(loomKey.isBlank() || loomKey.startsWith("sb_publishable_")) {
    "Use a publishable key, never a server secret key."
}

val generatedConfig = layout.buildDirectory.dir("generated/loom/kotlin")
val generateLoomConfig =
    tasks.register("generateLoomConfig") {
        inputs.property("url", loomUrl)
        inputs.property("publishableKey", loomKey)
        outputs.dir(generatedConfig)
        doLast {
            generatedConfig
                .get()
                .file("com/eyther/lumbridge/core/data/network/config/LoomBuildConfig.kt")
                .asFile
                .apply {
                    parentFile.mkdirs()
                    writeText(
                        "package com.eyther.lumbridge.core.data.network.config\ninternal object LoomBuildConfig {\n const val supabaseUrl = \"$loomUrl\"\n const val supabasePublishableKey = \"$loomKey\"\n}\n",
                    )
                }
        }
    }

kotlin.sourceSets.commonMain { kotlin.srcDir(generatedConfig) }

tasks.matching { it.name.startsWith("compile") }.configureEach { dependsOn(generateLoomConfig) }

compose.resources { packageOfResClass = "com.eyther.lumbridge.core.generated.resources" }
