plugins {
    id("learnwitme.android.library")
}

android {
    namespace = "com.learnwitme.benchmark"
}

dependencies {
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.junit)
    implementation(libs.androidx.junit.ext)
}
