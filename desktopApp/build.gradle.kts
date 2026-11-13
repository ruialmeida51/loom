plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(projects.shared.entrypoint)
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application { mainClass = "com.eyther.lumbridge.desktop.MainKt" }
}
