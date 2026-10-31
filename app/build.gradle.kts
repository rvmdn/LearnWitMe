plugins {
    id("learnwitme.android.application")
    id("learnwitme.android.compose")
    id("learnwitme.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.learnwitme.app"
    defaultConfig {
        applicationId = "com.learnwitme.app"
        versionCode = 2
        versionName = "1.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Core Modules
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":core:theme"))
    implementation(project(":core:config"))
    implementation(project(":core:network"))
    implementation(project(":core:storage"))
    implementation(project(":core:security"))
    implementation(project(":core:auth"))
    implementation(project(":core:logging"))
    implementation(project(":core:analytics"))
    implementation(project(":core:iap"))
    implementation(project(":core:plugin"))
    
    // Feature Modules
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:dailyfeed"))
    implementation(project(":feature:knowledgecard"))
    implementation(project(":feature:quiz"))
    implementation(project(":feature:mentor"))
    implementation(project(":feature:search"))
    
    // Developer Module
    implementation(project(":developer"))
    
    // AndroidX & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.timber)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation("org.json:json:20231013")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
