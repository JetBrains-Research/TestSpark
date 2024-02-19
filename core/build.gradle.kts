
fun properties(key: String) = project.findProperty(key).toString()

plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij")
}

group = "org.jetbrains.research"

repositories {
    mavenCentral()
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    compileOnly(kotlin("stdlib"))
}

// TODO: already configured in parent project, how to inherit it?
intellij {
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

tasks {
    runIde { enabled = false }
    runIdeForUiTests { enabled = false }
    buildSearchableOptions { enabled = false }

    patchPluginXml {
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
    }
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}