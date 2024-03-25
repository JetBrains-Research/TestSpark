package org.jetbrains.research.testspark.data

import com.intellij.util.xmlb.Converter
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NonNls


@Serializable
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

class JUnitVersionConverter : Converter<JUnitVersion>() {
    override fun fromString(value: String): JUnitVersion {
        return Json.decodeFromString(value)
    }

    override fun toString(value: JUnitVersion): String {
        return Json.encodeToString(value)
    }
}
