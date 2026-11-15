plugins {
    id("com.android.application") version "8.13.2" apply false
    kotlin("android") version "2.2.21" apply false
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.serialization") version "2.2.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21" apply false
}
// An optional ASCII output path avoids the JDK 17/Windows test-worker path encoding bug.
providers.gradleProperty("sf7bBuildRoot").orNull?.let { outputRoot ->
    allprojects { layout.buildDirectory.set(file("$outputRoot/${if (name == rootProject.name) "root" else name}")) }
}
