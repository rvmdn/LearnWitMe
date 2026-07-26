plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.compose")
    id("learnwitme.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.learnwitme.core.theme"
}

dependencies {
    implementation(project(":core:config"))
    implementation(libs.timber)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
