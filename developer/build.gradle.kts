plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.compose")
    id("learnwitme.android.hilt")
}

android {
    namespace = "com.learnwitme.developer"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:storage"))
    implementation(project(":core:logging"))
    implementation(project(":core:config"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
}
