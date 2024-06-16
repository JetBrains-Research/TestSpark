plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":langwrappers"))
    implementation(project(":core"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
