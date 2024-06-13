plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    // Define Java module specific dependencies here if any
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.24.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
