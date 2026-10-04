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
    }
}

rootProject.name = "TestMyNoetic"

include(":core")

// The Android app is only part of the build when an Android SDK is installed.
// That way the question engine and question bank (:core) can be built and
// tested on any machine with plain Java.
if (androidSdkAvailable(settingsDir)) {
    include(":app")
} else {
    println("No Android SDK found: building :core only.")
}

fun androidSdkAvailable(root: File): Boolean {
    val fromEnv = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    if (fromEnv != null && File(fromEnv).isDirectory) return true
    val localProps = File(root, "local.properties")
    if (!localProps.isFile) return false
    val sdkLine = localProps.readLines().firstOrNull { it.trim().startsWith("sdk.dir") } ?: return false
    return File(sdkLine.substringAfter("=").trim()).isDirectory
}
