plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.compose")
    id("learnwitme.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.learnwitme.core.designsystem"
}

dependencies {
    api(project(":core:theme"))
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
