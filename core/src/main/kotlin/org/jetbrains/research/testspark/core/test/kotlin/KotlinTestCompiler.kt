package org.jetbrains.research.testspark.core.test.kotlin

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.utils.CommandLineRunner

class KotlinTestCompiler(libPaths: List<String>, junitLibPaths: List<String>) :
    TestCompiler(libPaths, junitLibPaths) {

    private val log = KotlinLogging.logger { this::class.java }

    override fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String> {
        log.info { "[KotlinTestCompiler] Compiling ${path.substringAfterLast('/')}" }

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

        log.info { "Error message: '$errorMsg'" }

        // No need to save the .class file for kotlin, so checking the error message is enough
        return Pair(errorMsg.isBlank(), errorMsg)
    }
}
