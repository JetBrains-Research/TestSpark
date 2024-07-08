package org.jetbrains.research.testspark.core.test.kotlin

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import java.io.File

class KotlinTestCompiler(
    private val libPaths: List<String>,
    private val junitLibPaths: List<String>,
) : TestCompiler {

    private val log = KotlinLogging.logger { this::class.java }

    override fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String> {
        // Compile file
        val errorMsg = CommandLineRunner.run(
            arrayListOf(
                "kotlinc",
                "-cp",
                "\"${getPath(projectBuildPath)}\"",
                path,
            ),
        )

        log.info { "Error message: '$errorMsg'" }

        return Pair(File(path).exists(), errorMsg)
    }

    override fun getPath(buildPath: String): String {
        // create the path for the command
        val separator = DataFilesUtil.classpathSeparator
        val dependencyLibPath = libPaths.joinToString(separator.toString())
        val junitPath = junitLibPaths.joinToString(separator.toString())

        val path = "$junitPath${separator}$dependencyLibPath${separator}$buildPath"
        println("[KotlinTestCompiler]: the path is: $path")

        return path
    }
}
