package org.jetbrains.research.testspark.core.test.kotlin

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.test.ExecutionResult
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.utils.CommandLineRunner

class KotlinTestCompiler(libPaths: List<String>, junitLibPaths: List<String>) :
    TestCompiler(libPaths, junitLibPaths) {

    private val log = KotlinLogging.logger { this::class.java }

    override fun compileCode(path: String, projectBuildPath: String): ExecutionResult {
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
        log.info { "Exit code: '${executionResult.exitCode}'; Execution message: '${executionResult.executionMessage}'" }

        // TODO check for classfiles
        return executionResult
    }

    override fun getClassPaths(buildPath: String): String = commonPath.plus(buildPath)
}
