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

tasks.named("verifyPlugin") { enabled = false }
tasks.named("runIde") { enabled = false }
tasks.named("runPluginVerifier") { enabled = false }

kotlin {
    jvmToolchain(rootProject.properties["jvmToolchainVersion"].toString().toInt())
}
