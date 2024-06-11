plugins {
    java
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("stdlib"))

    implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")
}

sourceSets {
    main {
        java.srcDirs("src/org/jetbrains/research/testspark/java")
//        resources.srcDirs("resources")
    }
}
