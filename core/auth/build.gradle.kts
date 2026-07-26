plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.hilt")
}

android {
    namespace = "com.learnwitme.core.auth"
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:storage"))
    implementation(libs.timber)
}
