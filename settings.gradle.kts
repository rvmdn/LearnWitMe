pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "LearnWitMe"

// Include core modules
include(":app")
include(":core:domain")
include(":core:model")
include(":core:ui")
include(":core:network")
include(":core:storage")
include(":core:security")
include(":core:theme")
include(":core:designsystem")
include(":core:auth")
include(":core:iap")
include(":core:analytics")
include(":core:logging")
include(":core:config")
include(":core:navigation")
include(":core:plugin")

// Include feature modules
include(":feature:onboarding")
include(":feature:dailyfeed")
include(":feature:knowledgecard")
include(":feature:quiz")
include(":feature:mentor")
include(":feature:search")

// Include developer module
include(":developer")

// Include benchmark module
include(":benchmark")
