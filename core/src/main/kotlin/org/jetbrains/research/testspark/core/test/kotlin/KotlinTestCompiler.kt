package org.jetbrains.research.testspark.core.test

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.test.data.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import java.io.File

class KotlinTestCompiler(
    private val kotlinHomeDirectoryPath: String,
    private val libPaths: List<String>,
    private val junitLibPaths: List<String>,
) : TestCompiler {

    private val log = KotlinLogging.logger { this::class.java }

    override fun compileTestCases(
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

    override fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String> {

        // Find the kotlinc compiler
        val kotlinc = File(kotlinHomeDirectoryPath).walk()
            .filter {
                val isCompilerName = if (System.getProperty("os.name").toLowerCase().contains("win")) it.name.equals("kotlinc.bat") else it.name.equals("kotlinc")
                isCompilerName && it.isFile
            }
            .firstOrNull()

        if (kotlinc == null) {
            val msg = "Cannot find Kotlin compiler 'kotlinc' at '$kotlinHomeDirectoryPath'"
            throw RuntimeException(msg)
        }

        println("kotlinc found at '${kotlinc.absolutePath}'")

        // Compile file
        val processBuilder = ProcessBuilder(
            kotlinc.absolutePath,
            "-d", "\"${getPath(projectBuildPath)}\"",
            path
        )
        val process = processBuilder.start()
        val errorMsg = process.errorStream.bufferedReader().readText()

        process.waitFor()

        val compiledClassFilePath = path.replace(".kt", ".class")

        // Check if the .class file exists
        return Pair(File(compiledClassFilePath).exists(), errorMsg)
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
