package org.jetbrains.research.testspark.core.data

enum class JUnitVersion(
    val groupId: String,
    val version: Int,
    val libJar: Set<String>,
    val runWithAnnotationMeta: RunWithAnnotationMeta,
    val showName: String = "JUnit $version",
) {
    JUnit5(
        "org.junit.jupiter",
        5,
        setOf(
            "junit-jupiter-api-5.10.0.jar",
            "junit-jupiter-engine-5.10.0.jar",
            "junit-platform-commons-1.10.0.jar",
            "junit-platform-engine-1.10.0.jar",
            "junit-platform-launcher-1.10.0.jar",
        ),
        RunWithAnnotationMeta("ExtendWith", "import org.junit.jupiter.api.extension.ExtendWith;"),
    ),
    JUnit4(
        "junit",
        4,
        setOf("junit-4.13.jar"),
        RunWithAnnotationMeta("RunWith", "import org.junit.runner.RunWith;"),
    ),
}
