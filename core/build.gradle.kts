plugins {
    kotlin("jvm")
    `maven-publish`
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
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            artifactId = "testspark-core"
            version = "2.0.3"
            from(components["java"])
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
