package org.jetbrains.research.testspark.core.test

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.data.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import java.io.File

data class TestCasesCompilationResult(
    val allTestCasesCompilable: Boolean,
    val compilableTestCases: MutableSet<TestCaseGeneratedByLLM>,
)

class TestCompiler(
    val javaHomeDirectoryPath: String,
    val libPath: String,
    val junitVersion: JUnitVersion,
) {
    private val log = KotlinLogging.logger { this::class.java }

    /**
     * Compiles the generated files with test cases using the proper javac.
     *
     * @return true if all the provided test cases are successfully compiled,
     *         otherwise returns false.
     */
    fun compileTestCases(
        generatedTestCasesPaths: List<String>,
        buildPath: String,
        testCases: MutableList<TestCaseGeneratedByLLM>,
        // generatedTestData: TestGenerationData,
    ): TestCasesCompilationResult {
        var allTestCasesCompilable = true
        val compilableTestCases: MutableSet<TestCaseGeneratedByLLM> = mutableSetOf()

        for (index in generatedTestCasesPaths.indices) {
            val compilable = compileCode(generatedTestCasesPaths[index], buildPath).first
            allTestCasesCompilable = allTestCasesCompilable && compilable
            if (compilable) {
                // generatedTestData.compilableTestCases.add(testCases[index])
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
        val javaCompile = File(javaHomeDirectoryPath).walk()
            .filter {
                val isCompilerName = if (DataFilesUtil.isWindows()) it.name.equals("javac.exe") else it.name.equals("javac")
                isCompilerName && it.isFile
            }
            .first()

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

    /**
     * Generates the path for the command by concatenating the necessary paths.
     *
     * @param buildPath The path of the build file.
     * @return The generated path as a string.
     */
    fun getPath(buildPath: String): String {
        // create the path for the command
        val separator = DataFilesUtil.classpathSeparator
        val junitPath = junitVersion.libJar.joinToString(separator.toString()) { getLibrary(it) }
        val mockitoPath = getLibrary("mockito-core-5.0.0.jar")
        val hamcrestPath = getLibrary("hamcrest-core-1.3.jar")
        val byteBuddy = getLibrary("byte-buddy-1.14.6.jar")
        val byteBuddyAgent = getLibrary("byte-buddy-agent-1.14.6.jar")
        return "$junitPath${separator}$hamcrestPath${separator}$mockitoPath${separator}$byteBuddy${separator}$byteBuddyAgent${separator}$buildPath"
    }

    /**
     * Retrieves the absolute path of the specified library.
     *
     * @param libraryName the name of the library
     * @return the absolute path of the library
     */
    fun getLibrary(libraryName: String): String {
        return "${libPath.removeSurrounding("\"")}$libraryName"
    }
}
