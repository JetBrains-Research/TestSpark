package org.jetbrains.research.testspark.core.test

import org.jetbrains.research.testspark.core.test.data.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.core.utils.DataFilesUtil

data class TestCasesCompilationResult(
    val allTestCasesCompilable: Boolean,
    val compilableTestCases: MutableSet<TestCaseGeneratedByLLM>,
)

abstract class TestCompiler(
    private val libPaths: List<String>,
    private val junitLibPaths: List<String>,
) {
    /**
     * Compiles a list of test cases and returns the compilation result.
     *
     * @param generatedTestCasesPaths A list of file paths where the generated test cases are located.
     * @param buildPath All the directories where the compiled code of the project under test is saved. This path is used as a classpath to run each test case.
     * @param testCases A mutable list of `TestCaseGeneratedByLLM` objects representing the test cases to be compiled.
     * @return A `TestCasesCompilationResult` object containing the overall compilation success status and a set of compilable test cases.
     */
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
     * @param projectBuildPath All the directories where the compiled code of the project under test is saved. This path is used as a classpath to run each test case.
     * @return A pair containing a boolean value indicating whether the compilation was successful (true) or not (false),
     *         and a string message describing any error encountered during compilation.
     */
    abstract fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String>

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
