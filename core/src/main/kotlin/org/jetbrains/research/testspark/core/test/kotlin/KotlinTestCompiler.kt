package org.jetbrains.research.testspark.core.test.kotlin

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.exception.KotlinCompilerNotFoundException
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import java.io.File

class KotlinTestCompiler(
    libPaths: List<String>,
    junitLibPaths: List<String>,
    kotlinSDKHomeDirectory: String,
) : TestCompiler(libPaths, junitLibPaths) {
    private val logger = KotlinLogging.logger { this::class.java }
    private val kotlinc: String

    // init block to find the kotlinc compiler
    init {
        // search for a proper kotlinc
        val kotlinCompiler = File(kotlinSDKHomeDirectory).walk()
            .filter {
                /**
                 * Tested on Windows 10, IntelliJ IDEA Community Edition 2023.1.4 (2023.1.4.IC-231.9225.26)
                 *
                 * Windows' kotlinc requires `java` command to be present in ENV (e.g., present in PATH).
                 * Otherwise, it won't be able to execute itself.
                 *
                 * Missing `java` in PATH does not yield runtime error but is considered
                 * as failed compilation because `kotlinc` will complain about
                 * `java` command missing in PATH.
                 *
                 * TODO(vartiukhov): find a way to locate `java` on Windows
                 */
                val isCompilerName = if (DataFilesUtil.isWindows()) {
                    it.name.equals("kotlinc")
                } else {
                    it.name.equals("kotlinc")
                }
                isCompilerName && it.isFile
            }.firstOrNull()

        if (kotlinCompiler == null) {
            val msg = "Cannot find Kotlin compiler 'kotlinc' at $kotlinSDKHomeDirectory"
            logger.error { msg }
            throw KotlinCompilerNotFoundException("Please make sure that the Kotlin plugin is installed and enabled. $msg.")
        }

        kotlinc = kotlinCompiler.absolutePath
    }

    override fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String> {
        logger.info { "[KotlinTestCompiler] Compiling ${path.substringAfterLast('/')}" }

        val classPaths = "\"${getClassPaths(projectBuildPath)}\""
        // Compile file
        // TODO: we treat warnings as errors for now
        val errorMsg = CommandLineRunner.run(
            arrayListOf(
                kotlinc,
                "-cp",
                classPaths,
                path,
            ),
        )

        logger.info { "Error message: '$errorMsg'" }

        // No need to save the .class file for kotlin, so checking the error message is enough
        return Pair(errorMsg.isBlank(), errorMsg)
    }

    override fun getClassPaths(buildPath: String): String = commonPath.plus(buildPath)
}
