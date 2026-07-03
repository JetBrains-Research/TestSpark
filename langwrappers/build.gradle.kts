plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij.platform")
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {

    intellijPlatform {
        intellijIdeaCommunity(rootProject.properties["platformVersion"].toString(), useInstaller = false)
        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
        bundledPlugins(listOf("com.intellij.java"))

    }
    implementation(kotlin("stdlib"))

    implementation(project(":core"))
}

intellijPlatform {
    pluginConfiguration {
        rootProject.properties["platformVersion"]?.let { version = it.toString() }
    }
//    apply(plugin = "java")
//    // Apply more plugins if necessary
//    apply(plugin = "kotlin")
}

tasks.named("verifyPlugin") { enabled = false }
tasks.named("runIde") { enabled = false }
tasks.named("prepareJarSearchableOptions") { enabled = false }
tasks.named("publishPlugin") { enabled = false }

kotlin {
    jvmToolchain(rootProject.properties["jvmToolchainVersion"].toString().toInt())
}
