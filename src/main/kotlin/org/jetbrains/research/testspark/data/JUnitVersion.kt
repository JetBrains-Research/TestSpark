package org.jetbrains.research.testspark.data

enum class JUnitVersion(val groupId: String, val version: Int, val libJar: String, val showName: String = "JUnit $version") {
    JUnit5("org.junit.jupiter", 5, "junit-jupiter-api-5.10.0.jar"),
    JUnit4("junit", 4, "junit-4.13.jar"),
}
