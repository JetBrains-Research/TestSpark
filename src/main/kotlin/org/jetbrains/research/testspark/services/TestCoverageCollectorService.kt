package org.jetbrains.research.testspark.services

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtilRt
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.editor.Workspace
import org.jetbrains.research.testspark.tools.getBuildPath
import java.io.File
import java.util.UUID
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
     * Generates the path for the command by concatenating the necessary paths.
     *
     * @param buildPath The path of the build file.
     * @return The generated path as a string.
     */
    private fun getPath(buildPath: String): String {
        // create the path for the command
        val pluginsPath = PathManager.getPluginsPath()
        val junitPath = "\"$pluginsPath${sep}TestSpark${sep}lib${sep}junit-4.13.jar\""
        val mockitoPath = "\"$pluginsPath${sep}TestSpark${sep}lib${sep}mockito-core-5.0.0.jar\""
        val hamcrestPath = "\"$pluginsPath${sep}TestSpark${sep}lib${sep}hamcrest-core-1.3.jar\""
        val byteBuddy = "\"$pluginsPath${sep}TestSpark${sep}lib${sep}byte-buddy-1.14.6.jar\""
        val byteBuddyAgent = "\"$pluginsPath${sep}TestSpark${sep}lib${sep}byte-buddy-agent-1.14.6.jar\""
        return "$junitPath:$hamcrestPath:$mockitoPath:$byteBuddy:$byteBuddyAgent:$buildPath"
    }

    /**
     * Retrieves the absolute path of the specified library.
     *
     * @param libraryName the name of the library
     * @return the absolute path of the library
     */
    private fun getLibrary(libraryName: String): String {
        val pluginsPath = PathManager.getPluginsPath()
        return "\"$pluginsPath${sep}TestSpark${sep}lib${sep}$libraryName\""
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
        val errorMsg = project.service<RunCommandLineService>().runCommandLine(
            arrayListOf(
                javaCompile.absolutePath,
                "-cp",
                project.service<TestCoverageCollectorService>().getPath(projectBuildPath),
                path,
            ),
        )

        log.info("Error message: $errorMsg")

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

        // unique name
        var name = if (generatedTestPackage.isEmpty()) "" else "$generatedTestPackage."
        name += "$className#$testCaseName"

        // run the test method with jacoco agent
        val testExecutionError = project.service<RunCommandLineService>().runCommandLine(
            arrayListOf(
                javaRunner.absolutePath,
                "-javaagent:$jacocoAgentDir=destfile=$dataFileName.exec,append=false,includes=${project.service<Workspace>().classFQN}",
                "-cp",
                "${project.service<TestCoverageCollectorService>().getPath(projectBuildPath)
                }${project.service<TestCoverageCollectorService>().getLibrary("JUnitRunner.jar")}:$resultPath",
                "org.jetbrains.research.SingleJUnitTestRunner",
                name,
            ),
        )

        log.info("Test execution error message: $testExecutionError")

        // add passing test
        if (!getExceptionData(testExecutionError).first) {
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

        project.service<RunCommandLineService>().runCommandLine(command as ArrayList<String>)

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
     * Check for exception and collect lines covered during the exception happening.
     *
     * @param testExecutionError error output (including the thrown stack trace) during the test execution.
     * @return a set of lines that are covered in CUT during the exception happening.
     */
    fun getExceptionData(testExecutionError: String): Pair<Boolean, Set<Int>> {
        if (testExecutionError.isBlank()) {
            return Pair(false, emptySet())
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

        return Pair(Regex("(^\\d+\\) .+)|(^.+(Exception|Error): .+)|(^\\s+at .+)|(^\\s+... \\d+ more)|(^\\s*Caused by:.+)").find(testExecutionError) != null, result)
    }

    /**
     * Update the code of the test.
     *
     * @param testCode new code of test
     * @param testName the name of the test
     */
    fun updateDataWithTestCase(testCode: String, testName: String): TestCase {
        val fileName = "${project.service<JavaClassBuilderService>().getClassWithTestCaseName(testName)}.java"

        // get buildPath
        var buildPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        if (project.service<SettingsProjectService>().state.buildPath.isEmpty()) {
            // User did not set own path
            buildPath = getBuildPath(project)
        }

        // save new test to file
        val generatedTestPath: String = project.service<TestCoverageCollectorService>().saveGeneratedTests(
            project.service<Workspace>().testGenerationData.packageLine,
            testCode,
            project.service<Workspace>().resultPath!!,
            fileName,
        )

        // compilation checking
        if (!project.service<TestCoverageCollectorService>().compileCode(generatedTestPath, buildPath).first) {
            project.service<TestsExecutionResultService>().removeFromPassingTest(testName)
        } else {
            val dataFileName = "${project.service<Workspace>().resultPath!!}/jacoco-${fileName.split(".")[0]}"

            val testExecutionError = project.service<TestCoverageCollectorService>().createXmlFromJacoco(
                fileName.split(".")[0],
                dataFileName,
                testName,
                buildPath,
                project.service<Workspace>().testGenerationData.packageLine,
            )

            if (!File("$dataFileName.xml").exists()) {
                project.service<TestsExecutionResultService>().removeFromPassingTest(testName)
            } else {
                val testCase = project.service<TestCoverageCollectorService>().getTestCaseFromXml(
                    testName,
                    testCode,
                    project.service<TestCoverageCollectorService>().getExceptionData(testExecutionError).second,
                    "$dataFileName.xml",
                )

                project.service<Workspace>().cleanFolder(project.service<Workspace>().resultPath!!)

                return testCase
            }
        }
        project.service<Workspace>().cleanFolder(project.service<Workspace>().resultPath!!)

        return TestCase(testName, testCode, setOf(), setOf(), setOf())
    }
}
