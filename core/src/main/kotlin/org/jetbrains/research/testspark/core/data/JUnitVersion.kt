package org.jetbrains.research.testspark.core.data


enum class JUnitVersion(
    val groupId: String,
    val version: Int,
    val libJar: Set<JarLibraryDescriptor>,
    val runWithAnnotationMeta: RunWithAnnotationMeta,
    val showName: String = "JUnit $version",
) {
    JUnit5(
        "org.junit.jupiter",
        5,
        setOf(
            JarLibraryDescriptor(
                "junit-jupiter-api-5.10.0.jar",
                "https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-api/5.10.0/junit-jupiter-api-5.10.0.jar",
            ),
            JarLibraryDescriptor(
                "junit-jupiter-engine-5.10.0.jar",
                "https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-engine/5.10.0/junit-jupiter-engine-5.10.0.jar",
            ),
            JarLibraryDescriptor(
                "junit-platform-commons-1.10.0.jar",
                "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-commons/1.10.0/junit-platform-commons-1.10.0.jar",
            ),
            JarLibraryDescriptor(
                "junit-platform-engine-1.10.0.jar",
                "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-engine/1.10.0/junit-platform-engine-1.10.0.jar",
            ),
            JarLibraryDescriptor(
                "junit-platform-launcher-1.10.0.jar",
                "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-launcher/1.10.0/junit-platform-launcher-1.10.0.jar",
            ),
        ),
        RunWithAnnotationMeta("ExtendWith", "import org.junit.jupiter.api.extension.ExtendWith;"),
    ),
    JUnit4(
        "junit",
        4,
        setOf(
            JarLibraryDescriptor(
                "junit-4.13.jar",
                "https://repo1.maven.org/maven2/junit/junit/4.13/junit-4.13.jar",
            ),
        ),
        RunWithAnnotationMeta("RunWith", "import org.junit.runner.RunWith;"),
    ),
}
