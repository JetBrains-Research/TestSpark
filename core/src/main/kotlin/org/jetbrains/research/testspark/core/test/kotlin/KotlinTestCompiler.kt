package org.jetbrains.research.testspark.core.test.kotlin

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.utils.CommandLineRunner

class KotlinTestCompiler(libPaths: List<String>, junitLibPaths: List<String>) :
    TestCompiler(libPaths, junitLibPaths) {

    private val log = KotlinLogging.logger { this::class.java }

    override fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String> {
        log.info { "[KotlinTestCompiler] Compiling ${path.substringAfterLast('/')}" }

        // TODO find the kotlinc if it is not in PATH
        val classPaths = "\"${getClassPaths(projectBuildPath)}\""
        // Compile file
        val executionResult = CommandLineRunner.run(
            arrayListOf(
                "kotlinc",
                "-cp",
                classPaths,
                path,
            ),
        )
        val executionMsg = executionResult.second
        val execSuccessful = executionResult.first == 0

        if (!execSuccessful) {
            log.info { "Error message: '$executionMsg'" }
            if (executionMsg.contains("kotlinc: command not found'")) {
                throw RuntimeException(executionMsg)
            }
        }

        // TODO `.class` files are not saving for Kotlin
        return Pair(execSuccessful, executionMsg)
    }

    override fun getClassPaths(buildPath: String): String = commonPath.plus(buildPath)
}
