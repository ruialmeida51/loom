import java.util.Properties

plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    android {
        namespace = "com.eyther.lumbridge.shared.features.legal"
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
            implementation(libs.compose.resources)
        }
    }
}

val legalPropertiesFile = rootProject.file("config/loom-legal.properties")
val legalProperties = Properties().apply {
    legalPropertiesFile.reader(Charsets.UTF_8).use { load(it) }
}
val legalSite = legalProperties.getProperty("LEGAL_SITE_URL")
val policyVersion = legalProperties.getProperty("POLICY_VERSION")
require(Regex("https://[A-Za-z0-9./_-]+[A-Za-z0-9]").matches(legalSite)) { "Invalid legal site URL" }
require(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(policyVersion)) { "Invalid policy version" }
val generatedLegal = layout.buildDirectory.dir("generated/legal/kotlin")
val generateLegalConfiguration = tasks.register("generateLegalConfiguration") {
    inputs.file(legalPropertiesFile)
    outputs.dir(generatedLegal)
    doLast {
        generatedLegal.get().file("com/eyther/lumbridge/features/legal/data/config/LegalConfiguration.kt").asFile.apply {
            parentFile.mkdirs()
            writeText("""
                package com.eyther.lumbridge.features.legal.data.config

                internal object LegalConfiguration {
                    const val siteUrl = "$legalSite"
                    const val policyVersion = "$policyVersion"
                }
            """.trimIndent())
        }
    }
}
kotlin.sourceSets.commonMain { kotlin.srcDir(generatedLegal) }
tasks.matching { it.name.startsWith("compile") }.configureEach { dependsOn(generateLegalConfiguration) }
compose.resources { packageOfResClass = "com.eyther.lumbridge.features.legal.generated.resources" }
