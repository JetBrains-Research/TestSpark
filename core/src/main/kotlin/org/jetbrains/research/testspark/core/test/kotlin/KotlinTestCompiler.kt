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
        val errorMsg = CommandLineRunner.run(
            arrayListOf(
                "kotlinc",
                "-cp",
                classPaths,
                path,
            ),
        )

        if (errorMsg.isNotEmpty()) {
            log.info { "Error message: '$errorMsg'" }
            if (errorMsg.contains("kotlinc: command not found'")) {
                throw RuntimeException(errorMsg)
            }
        }

        // No need to save the .class file for kotlin, so checking the error message is enough
        return Pair(errorMsg.isBlank(), errorMsg)
    }

    override fun getClassPaths(buildPath: String): String = commonPath.plus(buildPath)
}
