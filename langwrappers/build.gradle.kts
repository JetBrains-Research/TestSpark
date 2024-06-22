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

    implementation(project(":core"))
}

intellij {
    rootProject.properties["platformVersion"]?.let { version.set(it.toString()) }
    plugins.set(listOf("java"))
    downloadSources.set(true)
}

kotlin {
    jvmToolchain(17)
}
