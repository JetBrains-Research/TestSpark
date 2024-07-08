package org.jetbrains.research.testspark.core.test

import org.jetbrains.research.testspark.core.test.data.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.core.test.strategies.TestCompilerStrategy
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import org.jetbrains.research.testspark.core.utils.Language

data class TestCasesCompilationResult(
    val allTestCasesCompilable: Boolean,
    val compilableTestCases: MutableSet<TestCaseGeneratedByLLM>,
)

class TestCompiler(
    private val javaHomeDirectoryPath: String,
    private val libPaths: List<String>,
    private val junitLibPaths: List<String>,
    private val language: Language,
) {
    fun compileTestCases(
        generatedTestCasesPaths: List<String>,
        buildPath: String,
        testCases: MutableList<TestCaseGeneratedByLLM>,
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
        return when (language) {
            Language.Java -> TestCompilerStrategy.compileJavaCode(
                path,
                "\"${getClassPaths(projectBuildPath)}\"",
                javaHomeDirectoryPath
            )

            Language.Kotlin -> TestCompilerStrategy.compileKotlinCode(
                path,
                "\"${getClassPaths(projectBuildPath)}\""
            )
        }
    }

    /**
     * Generates the path for the command by concatenating the necessary paths.
     *
     * @param buildPath The path of the build file.
     * @return The generated path as a string.
     */
    fun getClassPaths(buildPath: String): String {
        // create the path for the command
        val separator = DataFilesUtil.classpathSeparator
        val dependencyLibPath = libPaths.joinToString(separator.toString())
        val junitPath = junitLibPaths.joinToString(separator.toString())

        val path = "$junitPath${separator}$dependencyLibPath${separator}$buildPath"
        println("[TestCompiler]: the path is: $path")

        return path
    }
}
