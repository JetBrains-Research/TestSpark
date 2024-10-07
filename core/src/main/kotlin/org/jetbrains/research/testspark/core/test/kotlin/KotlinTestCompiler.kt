package org.jetbrains.research.testspark.core.test.kotlin

import io.github.oshai.kotlinlogging.KotlinLogging
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
        // TODO: does it work on Windows?
        // search for a proper kotlinc
        val kotlinCompiler = File(kotlinSDKHomeDirectory).walk()
            .filter {
                val isCompilerName = if (DataFilesUtil.isWindows()) {
                    // TODO: is it kotlinc.exe?
                    it.name.equals("kotlinc.exe")
                } else {
                    it.name.equals("kotlinc")
                }
                isCompilerName && it.isFile
            }.firstOrNull()

        if (kotlinCompiler == null) {
            val msg = "Cannot find Kotlinc compiler 'kotlinc' at '$kotlinSDKHomeDirectory'"
            logger.error { msg }
            throw RuntimeException(msg)
        }
        kotlinc = kotlinCompiler.absolutePath
    }

    override fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String> {
        logger.info { "[KotlinTestCompiler] Compiling ${path.substringAfterLast('/')}" }

        val classPaths = "\"${getClassPaths(projectBuildPath)}\""
        // Compile file
        val errorMsg = CommandLineRunner.run(
            arrayListOf(
                kotlinc,
                "-cp",
                classPaths,
                path,
            ),
        )

        // TODO: we treat warnings as errors for now
        if (errorMsg.isNotEmpty()) {
            logger.info { "Error message: '$errorMsg'" }
            if (errorMsg.contains("kotlinc: command not found'")) {
                throw RuntimeException(errorMsg)
            }
        }

        // No need to save the .class file for kotlin, so checking the error message is enough
        return Pair(errorMsg.isBlank(), errorMsg)
    }

    override fun getClassPaths(buildPath: String): String = commonPath.plus(buildPath)
}
