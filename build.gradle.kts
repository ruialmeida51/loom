plugins {
    id("com.google.gms.google-services") version "4.5.0" apply false
    alias(libs.plugins.kmp) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.koin.compiler) apply false
}
