plugins {
    kotlin("jvm")
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
        java.srcDirs("src/org/jetbrains/research/testspark/kotlin")
//        resources.srcDirs("resources")
    }
}
