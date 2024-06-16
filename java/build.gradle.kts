plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij")
}

repositories {
    mavenCentral()
    // Add any other repositories you need
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(project(":langwrappers"))
    implementation(project(":core"))
}

intellij {
    version.set("2024.1")
    plugins.set(listOf("java"))
    downloadSources.set(true)
}

kotlin {
    jvmToolchain(17)
}