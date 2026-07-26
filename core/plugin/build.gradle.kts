plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.compose")
    id("learnwitme.android.hilt")
}

android {
    namespace = "com.learnwitme.core.plugin"
}

dependencies {
    api(project(":core:ui"))
    api(project(":core:navigation"))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
}
