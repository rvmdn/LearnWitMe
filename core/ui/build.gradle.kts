plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.compose")
    id("learnwitme.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.learnwitme.core.ui"
}

dependencies {
    api(project(":core:theme"))
    api(project(":core:domain"))
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.junit)
}
