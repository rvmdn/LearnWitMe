plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.hilt")
}

android {
    namespace = "com.learnwitme.core.analytics"
}

dependencies {
    implementation(project(":core:domain"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
