package org.jetbrains.research.testspark.services

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtilRt
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.editor.Workspace
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

class TestCoverageCollectorService(private val project: Project) {
    private val sep = File.separatorChar

    private val id = UUID.randomUUID().toString()
    val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testSparkResults$sep"
    val testResultName = "test_gen_result_$id"

    val resultPath = "$testResultDirectory$testResultName"

    private val javaHomeDirectory = ProjectRootManager.getInstance(project).projectSdk!!.homeDirectory!!

    private val log = Logger.getInstance(this::class.java)

    /**
     * Executes a command line process and returns the output as a string.
     *
     * @param cmd The command line arguments as an ArrayList of strings.
     * @return The output of the command line process as a string.
     */
    private fun runCommandLine(cmd: ArrayList<String>): String {
        var errorMessage = ""

        val process = ProcessBuilder()
            .command("bash", "-c", cmd.joinToString(" "))
            .redirectErrorStream(true)
            .start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            errorMessage += line
        }

        process.waitFor()

        return errorMessage
    }

    /**
     * Generates the path for the command by concatenating the necessary paths.
     *
     * @param buildPath The path of the build file.
     * @return The generated path as a string.
     */
    private fun getPath(buildPath: String): String {
        // create the path for the command
        val pluginsPath = System.getProperty("idea.plugins.path")
        val junitPath = "$pluginsPath${sep}TestSpark${sep}lib${sep}junit-4.13.jar"
        val mockitoPath = "$pluginsPath${sep}TestSpark${sep}lib${sep}mockito-core-5.0.0.jar"
        val hamcrestPath = "$pluginsPath${sep}TestSpark${sep}lib${sep}hamcrest-core-1.3.jar"
        return "$junitPath:$hamcrestPath:$mockitoPath:$buildPath"
    }

    /**
     * Retrieves the absolute path of the specified library.
     *
     * @param libraryName the name of the library
     * @return the absolute path of the library
     */
    private fun getLibrary(libraryName: String): String {
        val pluginsPath = System.getProperty("idea.plugins.path")
        return "$pluginsPath${sep}TestSpark${sep}lib${sep}$libraryName"
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
        val javaCompile = File(javaHomeDirectory.path).walk().filter { it.name.equals("javac") && it.isFile }.first()
        // compile file
        val errorMsg = runCommandLine(
            arrayListOf(
                javaCompile.absolutePath,
                "-cp",
                project.service<TestCoverageCollectorService>().getPath(projectBuildPath),
                path,
            ),
        )

        // create .class file path
        val classFilePath = path.replace(".java", ".class")

        // check is .class file exists
        return Pair(File(classFilePath).exists(), errorMsg)
    }

    /**
     * Save the generated tests to a specified directory.
     *
     * @param packageString The package string where the generated tests will be saved.
     * @param code The generated test code.
     * @param resultPath The result path where the generated tests will be saved.
     * @param testFileName The name of the test file.
     * @return The path where the generated tests are saved.
     */
    fun saveGeneratedTests(packageString: String, code: String, resultPath: String, testFileName: String): String {
        // Generate the final path for the generated tests
        var generatedTestPath = "$resultPath${File.separatorChar}"
        packageString.split(".").forEach { directory ->
            if (directory.isNotBlank()) generatedTestPath += "$directory${File.separatorChar}"
        }
        Path(generatedTestPath).createDirectories()

        // Save the generated test suite to the file
        val testFile = File("$generatedTestPath$testFileName")
        testFile.createNewFile()
        log.info("Save test in file " + testFile.absolutePath)
        testFile.writeText(code)

        return "$generatedTestPath$testFileName"
    }

    /**
     * Creates an XML report from the JaCoCo coverage data for a specific test case.
     *
     * @param className The name of the class under test.
     * @param dataFileName The name of the coverage data file.
     * @param testCaseName The name of the test case.
     * @param projectBuildPath The build path of the project.
     * @param generatedTestPackage The package where the generated test class is located.
     * @return An empty string if the test execution is successful, otherwise an error message.
     */
    fun createXmlFromJacoco(
        className: String,
        dataFileName: String,
        testCaseName: String,
        projectBuildPath: String,
        generatedTestPackage: String,
    ): String {
        // find the proper javac
        val javaRunner = File(javaHomeDirectory.path).walk().filter { it.name.equals("java") && it.isFile }.first()
        // JaCoCo libs
        val jacocoAgentDir = project.service<TestCoverageCollectorService>().getLibrary("jacocoagent.jar")
        val jacocoCLIDir = project.service<TestCoverageCollectorService>().getLibrary("jacococli.jar")
        val sourceRoots = ModuleRootManager.getInstance(project.service<Workspace>().cutModule!!).getSourceRoots(false)

        // run the test method with jacoco agent
        val testExecutionError = runCommandLine(
            arrayListOf(
                javaRunner.absolutePath,
                "-javaagent:$jacocoAgentDir=destfile=$dataFileName.exec,append=false,includes=${project.service<Workspace>().classFQN}",
                "-cp",
                "${
                    project.service<TestCoverageCollectorService>().getPath(projectBuildPath)
                }${project.service<TestCoverageCollectorService>().getLibrary("JUnitRunner.jar")}:$resultPath",
                "org.jetbrains.research.SingleJUnitTestRunner",
                "$generatedTestPackage$className#$testCaseName",
            ),
        )

        // add passing test
        if (testExecutionError.isEmpty()) {
            project.service<TestsExecutionResultService>().addPassingTest(testCaseName)
        } else {
            project.service<TestsExecutionResultService>().removeFromPassingTest(testCaseName)
        }

        // Prepare the command for generating the Jacoco report
        val command = mutableListOf(
            javaRunner.absolutePath,
            "-jar",
            jacocoCLIDir,
            "report",
            "$dataFileName.exec",
        )

        // for classpath containing cut
        command.add("--classfiles")
        command.add(CompilerModuleExtension.getInstance(project.service<Workspace>().cutModule!!)?.compilerOutputPath!!.path)

        // for each source folder
        sourceRoots.forEach { root ->
            command.add("--sourcefiles")
            command.add(root.path)
        }

        // generate XML report
        command.add("--xml")
        command.add("$dataFileName.xml")

        log.info("Runs command: ${command.joinToString(" ")}")

        runCommandLine(command as ArrayList<String>)

        return testExecutionError
    }

    /**
     * Saves data of a given test case to a report.
     *
     * @param testCaseName The test case name.
     * @param testCaseCode The test case code.
     * @param xmlFileName The XML file name to read data from.
     */
    fun getTestCaseFromXml(
        testCaseName: String,
        testCaseCode: String,
        linesCoveredDuringTheException: Set<Int>,
        xmlFileName: String,
    ): TestCase {
        val setOfLines = mutableSetOf<Int>()
        var isCorrectSourceFile: Boolean
        File(xmlFileName).readText().konsumeXml().apply {
            children("report") {
                children("sessioninfo") {}
                children("package") {
                    children("class") {
                        children("method") {
                            children("counter") {}
                        }
                        children("counter") {}
                    }
                    children("sourcefile") {
                        isCorrectSourceFile =
                            this.attributes.getValue("name") == project.service<Workspace>().fileUrl!!.split(File.separatorChar)
                                .last()
                        children("line") {
                            if (isCorrectSourceFile && this.attributes.getValue("mi") == "0") {
                                setOfLines.add(this.attributes.getValue("nr").toInt())
                            }
                        }
                        children("counter") {}
                    }
                    children("counter") {}
                }
                children("counter") {}
            }
        }

        log.info("Test case saved:\n$testCaseName")

        // Add lines that Jacoco might have missed because of its limitation during the exception
        setOfLines.addAll(linesCoveredDuringTheException)

        return TestCase(testCaseName, testCaseCode, setOfLines, setOf(), setOf())
    }

    /**
     * Collect lines covered during the exception happening.
     *
     * @param testExecutionError error output (including the thrown stack trace) during the test execution.
     * @return a set of lines that are covered in CUT during the exception happening.
     */
    fun collectLinesCoveredDuringException(testExecutionError: String): Set<Int> {
        if (testExecutionError.isBlank()) {
            return emptySet()
        }

        val result = mutableSetOf<Int>()

        // get frames
        val frames = testExecutionError.split("\n\tat ").toMutableList()
        frames.removeFirst()

        frames.forEach { frame ->
            if (frame.contains(project.service<Workspace>().classFQN!!)) {
                val coveredLineNumber = frame.split(":")[1].replace(")", "").toIntOrNull()
                if (coveredLineNumber != null) {
                    result.add(coveredLineNumber)
                }
            }
        }

        return result
    }
}
