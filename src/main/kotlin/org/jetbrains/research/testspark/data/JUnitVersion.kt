package org.jetbrains.research.testspark.data

enum class JUnitVersion(
    val groupId: String,
    val version: Int,
    val libJar: String,
    val runWithAnnotationMeta: RunWithAnnotationMeta,
    val showName: String = "JUnit $version",
) {
    JUnit5(
        "org.junit.jupiter",
        5,
        "junit-jupiter-api-5.10.0.jar",
        RunWithAnnotationMeta("ExtendWith", "import org.junit.jupiter.api.extension.ExtendWith;"),
    ),
    JUnit4(
        "junit",
        4,
        "junit-4.13.jar",
        RunWithAnnotationMeta("RunWith", "import org.junit.runner.RunWith;"),
    ),
}
