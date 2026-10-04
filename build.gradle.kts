// All Gradle plugins live on the root build-script classpath so the Kotlin
// and Android plugins share one class loader. The Android Gradle plugin is
// only added when settings.gradle.kts included the :app module.
buildscript {
    val kotlinVersion = "2.0.21"
    val agpVersion = "8.7.3"
    val withAndroid = rootProject.findProject(":app") != null
    repositories {
        if (withAndroid) google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
        if (withAndroid) {
            classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:$kotlinVersion")
            classpath("com.android.tools.build:gradle:$agpVersion")
        }
    }
}
