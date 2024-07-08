package org.jetbrains.research.testspark.core.test.strategies

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import java.io.File

class TestCompilerStrategy {

    companion object {
        private val log = KotlinLogging.logger { this::class.java }

        fun compileJavaCode(
            path: String,
            javaHomeDirectoryPath: String,
            classPaths: String
        ): Pair<Boolean, String> {
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

        fun compileKotlinCode(path: String, classPaths: String): Pair<Boolean, String> {
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

            return Pair(File(path).exists(), errorMsg)
        }
    }
}