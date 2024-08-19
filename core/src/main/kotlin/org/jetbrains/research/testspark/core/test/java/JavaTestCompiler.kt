package org.jetbrains.research.testspark.core.test.java

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import java.io.File

class JavaTestCompiler(
    libPaths: List<String>,
    junitLibPaths: List<String>,
    private val javaHomeDirectoryPath: String,
) : TestCompiler(libPaths, junitLibPaths) {

    private val log = KotlinLogging.logger { this::class.java }

    override fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String> {
        val classPaths = "\"${getClassPaths(projectBuildPath)}\""
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
                classPaths,
                path,
            ),
        )

        log.info { "Error message: '$errorMsg'" }
        // create .class file path
        val classFilePath = path.replace(".java", ".class")

        // check is .class file exists
        return Pair(File(classFilePath).exists(), errorMsg)
    }
}
