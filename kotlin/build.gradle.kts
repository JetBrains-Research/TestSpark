plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    // Define Kotlin module specific dependencies here if any
    implementation(kotlin("stdlib"))
    // Kotlin PSI and IDE integration dependencies
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable-common:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable-common-27:1.5.21")

    // IntelliJ IDEA Kotlin plugin dependencies
    implementation("org.jetbrains.kotlin:kotlin-psi:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-psi-api:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-ide-common:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-refactoring:1.5.21")

    // IntelliJ IDEA dependencies
    implementation("com.intellij:intellij-annotations:22.1.0")

    implementation("org.jetbrains.kotlin:kotlin-test:1.8.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
