import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":langwrappers"))
    // Define Kotlin module specific dependencies here if any
    implementation(kotlin("stdlib"))
    // Kotlin PSI and IDE integration dependencies

    // IntelliJ IDEA Kotlin plugin dependencies
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-refactoring:1.5.21")

    // IntelliJ IDEA dependencies

    implementation("org.jetbrains.kotlin:kotlin-test:1.8.0")
    implementation("org.jetbrains.kotlin", "kotlin-compiler")
    implementation("org.jetbrains.kotlin", "kotlin-compiler-embeddable")
    implementation("org.jetbrains.kotlin", "kotlin-reflect")
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}