plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.hilt")
}

android {
    namespace = "com.learnwitme.core.security"
}

dependencies {
    implementation(libs.androidx.security.crypto)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite.ktx)
}
