plugins {
    id("learnwitme.android.library")
    id("learnwitme.android.compose")
    id("learnwitme.android.hilt")
}
android {
    namespace = "com.learnwitme.feature.knowledgecard"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
