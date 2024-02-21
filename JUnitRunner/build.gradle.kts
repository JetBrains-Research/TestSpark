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

    implementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    implementation("org.junit.platform:junit-platform-launcher:1.10.0")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}
