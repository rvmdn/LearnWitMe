plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.hilt")
}

android {
    namespace = "com.learnwitme.core.logging"
}

dependencies {
    implementation(libs.timber)
}
