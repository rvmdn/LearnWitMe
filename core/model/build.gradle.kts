plugins {
    id("learnwitme.android.library")
    alias(libs.plugins.kotlin.serialization)
}
android {
    namespace = "com.learnwitme.core.model"
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.kotlinx.serialization.json)
}
