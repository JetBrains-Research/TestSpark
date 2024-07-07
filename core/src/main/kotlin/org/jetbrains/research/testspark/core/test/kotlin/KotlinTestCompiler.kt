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
        // find the proper kotlinc
        val kotlinCompile = File(kotlinHomeDirectoryPath).walk()
            .filter {
                val isCompilerName = if (DataFilesUtil.isWindows()) it.name.equals("kotlinc.bat") else it.name.equals("kotlinc")
                isCompilerName && it.isFile
            }
            .firstOrNull()

        if (kotlinCompile == null) {
            val msg = "Cannot find Kotlin compiler 'kotlinc' at '$kotlinHomeDirectoryPath'"
            log.error { msg }
            throw RuntimeException(msg)
        }

        println("kotlinc found at '${kotlinCompile.absolutePath}'")

        // compile file
        val errorMsg = CommandLineRunner.run(
            arrayListOf(
                kotlinCompile.absolutePath,
                "-cp",
                getPath(projectBuildPath),
                path,
            ),
        )

        log.info { "Error message: '$errorMsg'" }

        // create .class file path
        val classFilePath = path.replace(".kt", ".class")

        // check if .class file exists
        return Pair(File(classFilePath).exists(), errorMsg)
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
