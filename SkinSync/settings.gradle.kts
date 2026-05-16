pluginManagement {
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

rootProject.name = "SkinSync"

include(":app")
include(":domain")
include(":core:color")
include(":core:designsystem")
include(":core:ml")
include(":data")
include(":feature:capture")
include(":feature:profile")
include(":feature:result")
include(":feature:history")
include(":feature:wardrobe")
