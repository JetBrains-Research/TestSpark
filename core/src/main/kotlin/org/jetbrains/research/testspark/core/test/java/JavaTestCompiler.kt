package org.jetbrains.research.testspark.core.test.java

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import java.io.File

class JavaTestCompiler(
    private val javaHomeDirectoryPath: String,
    private val libPaths: List<String>,
    private val junitLibPaths: List<String>,
) : TestCompiler {
    private val log = KotlinLogging.logger { this::class.java }

    override fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String> {
        // find the proper javac
        val javaCompile = File(javaHomeDirectoryPath).walk()
            .filter {
                val isCompilerName =
                    if (DataFilesUtil.isWindows()) it.name.equals("javac.exe") else it.name.equals("javac")
                isCompilerName && it.isFile
            }
            .firstOrNull()

        if (javaCompile == null) {
            val msg = "Cannot find java compiler 'javac' at '$javaHomeDirectoryPath'"
            log.error { msg }
            throw RuntimeException(msg)
        }

        println("javac found at '${javaCompile.absolutePath}'")

        // compile file
        val errorMsg = CommandLineRunner.run(
            arrayListOf(
                javaCompile.absolutePath,
                "-cp",
                "\"${getPath(projectBuildPath)}\"",
                path,
            ),
        )

        log.info { "Error message: '$errorMsg'" }
        // create .class file path
        val classFilePath = path.replace(".java", ".class")

        // check is .class file exists
        return Pair(File(classFilePath).exists(), errorMsg)
    }

    override fun getPath(buildPath: String): String {
        // create the path for the command
        val separator = DataFilesUtil.classpathSeparator
        val dependencyLibPath = libPaths.joinToString(separator.toString())
        val junitPath = junitLibPaths.joinToString(separator.toString())

        val path = "$junitPath${separator}$dependencyLibPath${separator}$buildPath"
        println("[TestCompiler]: the path is: $path")

        return path
    }
}
