plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.compose")
    id("learnwitme.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.learnwitme.core.navigation"
}

dependencies {
    api(project(":core:config"))
    api(libs.androidx.navigation.compose)
    api(libs.androidx.hilt.navigation.compose)
    implementation(libs.timber)
    implementation(libs.kotlinx.serialization.json)
}
