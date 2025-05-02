plugins {
    kotlin("jvm") version "2.1.0"
    `maven-publish`
    kotlin("plugin.serialization") version "2.1.0"
}

group = "org.jetbrains.research"
val spaceUsername =
    System.getProperty("space.username")?.toString() ?: project.properties["spaceUsername"]?.toString() ?: ""
val spacePublish =
    System.getProperty("space.publish")?.toString() ?: project.properties["spacePublish"]?.toString() ?: ""

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    compileOnly(kotlin("stdlib"))

    implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    val ktorVersion = "2.3.13"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.12.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(rootProject.properties["jvmToolchainVersion"].toString().toInt())
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.named("javadoc"))
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            artifactId = "testspark-core"
            version = "5.0.1"
            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                inceptionYear.set("2024")
                name.set(project.name)
                description.set(project.description)
                packaging = "jar"

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("https://packages.jetbrains.team/maven/p/automatically-generating-unit-tests/public")
            credentials {
                username = spaceUsername
                password = spacePublish
            }
        }
    }
}
