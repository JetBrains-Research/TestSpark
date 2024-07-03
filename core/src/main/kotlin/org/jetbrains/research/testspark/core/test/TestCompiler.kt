package org.jetbrains.research.testspark.core.test

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.test.data.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import java.io.File

data class TestCasesCompilationResult(
    val allTestCasesCompilable: Boolean,
    val compilableTestCases: MutableSet<TestCaseGeneratedByLLM>,
)


class TestCompiler (
    private val kotlinHomeDirectoryPath: String,
    private val libPaths: List<String>,
    private val junitLibPaths: List<String>,
) {
    private val log = KotlinLogging.logger { this::class.java }

    fun compileTestCases(
        generatedTestCasesPaths: List<String>,
        buildPath: String,
        testCases: MutableList<TestCaseGeneratedByLLM>
    ): TestCasesCompilationResult {
        var allTestCasesCompilable = true
        val compilableTestCases: MutableSet<TestCaseGeneratedByLLM> = mutableSetOf()

        for (index in generatedTestCasesPaths.indices) {
            val compilable = compileCode(generatedTestCasesPaths[index], buildPath).first
            allTestCasesCompilable = allTestCasesCompilable && compilable
            if (compilable) {
                compilableTestCases.add(testCases[index])
            }
        }

        return TestCasesCompilationResult(allTestCasesCompilable, compilableTestCases)
    }

    /**
     * Compiles the code at the specified path using the provided project build path.
     *
     * @param path The path of the code file to compile.
     * @param projectBuildPath The project build path to use during compilation.
     * @return A pair containing a boolean value indicating whether the compilation was successful (true) or not (false),
     *         and a string message describing any error encountered during compilation.
     */
    fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String> {
        // find the proper javac
        val javaCompile = File(kotlinHomeDirectoryPath).walk()
            .filter {
                val isCompilerName = if (DataFilesUtil.isWindows()) it.name.equals("javac.exe") else it.name.equals("javac")
                isCompilerName && it.isFile
            }
            .firstOrNull()

        if (javaCompile == null) {
            val msg = "Cannot find java compiler 'javac' at '$kotlinHomeDirectoryPath'"
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
        val classFilePath = path.replace(".kt", ".class")

        // check if .class file exists
        return Pair(File(classFilePath).exists(), errorMsg)
    }

    /**
     * Generates the path for the command by concatenating the necessary paths.
     *
     * @param buildPath The path of the build file.
     * @return The generated path as a string.
     */
    fun getPath(buildPath: String): String {
        // create the path for the command
        val separator = DataFilesUtil.classpathSeparator
        val dependencyLibPath = libPaths.joinToString(separator.toString())
        val junitPath = junitLibPaths.joinToString(separator.toString())

        val path = "$junitPath${separator}$dependencyLibPath${separator}$buildPath"
        println("[TestCompiler]: the path is: $path")

        return path
    }
}
