package org.jetbrains.research.testspark.core.utils

val javaImportPattern =
    Regex(
        pattern = "^import\\s+(static\\s)?((?:[a-zA-Z_]\\w*\\.)*[a-zA-Z_](?:\\w*\\.?)*)(?:\\.\\*)?;",
        options = setOf(RegexOption.MULTILINE),
    )

/**
 * Parse all the possible Kotlin import patterns
 *
 * import org.mockito.Mockito.`when`
 * import kotlin.math.cos
 * import kotlin.math.*
 * import kotlin.math.PI as piValue
 */
val kotlinImportPattern =
    Regex(
        pattern = "^import\\s+((?:[a-zA-Z_]\\w*\\.)*(?:\\w*\\.?)*)?(\\*)?( as \\w*)?(`\\w*`)?",
        options = setOf(RegexOption.MULTILINE),
    )

val javaPackagePattern =
    Regex(
        pattern = "^package\\s+((?:[a-zA-Z_]\\w*\\.)*[a-zA-Z_](?:\\w*\\.?)*)(?:\\.\\*)?;",
        options = setOf(RegexOption.MULTILINE),
    )

val kotlinPackagePattern =
    Regex(
        pattern = "^package\\s+((?:[a-zA-Z_]\\w*\\.)*[a-zA-Z_](?:\\w*\\.?)*)(?:\\.\\*)?",
        options = setOf(RegexOption.MULTILINE),
    )
