plugins {
    id("java")
}

group = "org.jetbrains.research"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("junit:junit:4.13")
}

tasks.test {
    useJUnitPlatform()
}
