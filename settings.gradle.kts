pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // JitPack è necessario per librerie esterne come MPAndroidChart
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "PushApp"
include(":app")