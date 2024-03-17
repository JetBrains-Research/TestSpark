package org.jetbrains.research.testspark.services

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtilRt
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import org.jetbrains.research.testspark.tools.getBuildPath
import org.jetbrains.research.testspark.core.test.data.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import java.io.File
import java.util.UUID
import kotlin.collections.ArrayList
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

@Service(Service.Level.PROJECT)
class TestStorageProcessingService(private val project: Project) {
    private val sep = File.separatorChar

    private val id = UUID.randomUUID().toString()
    val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testSparkResults$sep"
    val testResultName = "test_gen_result_$id"

    val resultPath = "$testResultDirectory$testResultName"

    private val javaHomeDirectory = ProjectRootManager.getInstance(project).projectSdk!!.homeDirectory!!

    private val log = Logger.getInstance(this::class.java)

    private val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

    /**
     * Generates the path for the command by concatenating the necessary paths.
     *
     * @param buildPath The path of the build file.
     * @return The generated path as a string.
     */
    private fun getPath(buildPath: String): String {
        // create the path for the command
        val junitVersion = settingsState.junitVersion
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
        val javaCompile = File(javaHomeDirectory.path).walk()
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
                getPath(projectBuildPath),
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
     * Compiles the generated test file using the proper javac and returns a Pair
     * indicating whether the compilation was successful and any error message encountered during compilation.
     *
     * @return A Pair containing a boolean indicating whether the compilation was successful
     *         and a String containing any error message encountered during compilation.
     */
    fun compileTestCases(generatedTestCasesPaths: List<String>, buildPath: String, testCases: MutableList<TestCaseGeneratedByLLM>): Boolean {
        var result = false
        for (index in generatedTestCasesPaths.indices) {
            val compilable = compileCode(generatedTestCasesPaths[index], buildPath).first
            result = result || compilable
            if (compilable) {
                project.service<TestGenerationDataService>().compilableTestCases.add(testCases[index])
            }
        }
        return result
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
    fun saveGeneratedTest(packageString: String, code: String, resultPath: String, testFileName: String): String {
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
    private fun createXmlFromJacoco(
        className: String,
        dataFileName: String,
        testCaseName: String,
        projectBuildPath: String,
        generatedTestPackage: String,
    ): String {
        // find the proper javac
        val javaRunner = File(javaHomeDirectory.path).walk()
            .filter {
                val isJavaName = if (DataFilesUtil.isWindows()) it.name.equals("java.exe") else it.name.equals("java")
                isJavaName && it.isFile
            }
            .first()
        // JaCoCo libs
        val jacocoAgentDir = getLibrary("jacocoagent.jar")
        val jacocoCLIDir = getLibrary("jacococli.jar")
        val sourceRoots = ModuleRootManager.getInstance(project.service<ProjectContextService>().cutModule!!).getSourceRoots(false)

        // unique name
        var name = if (generatedTestPackage.isEmpty()) "" else "$generatedTestPackage."
        name += "$className#$testCaseName"

        val junitVersion = settingsState.junitVersion.version

        // run the test method with jacoco agent
        val testExecutionError = CommandLineRunner.run(
            arrayListOf(
                javaRunner.absolutePath,
                "-javaagent:$jacocoAgentDir=destfile=$dataFileName.exec,append=false,includes=${project.service<ProjectContextService>().classFQN}",
                "-cp",
                "${getPath(projectBuildPath)}${getLibrary("JUnitRunner.jar")}${DataFilesUtil.classpathSeparator}$resultPath",
                "org.jetbrains.research.SingleJUnitTestRunner$junitVersion",
                name,
            ),
        )

        log.info("Test execution error message: $testExecutionError")

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
        command.add(CompilerModuleExtension.getInstance(project.service<ProjectContextService>().cutModule!!)?.compilerOutputPath!!.path)

        // for each source folder
        sourceRoots.forEach { root ->
            command.add("--sourcefiles")
            command.add(root.path)
        }

        // generate XML report
        command.add("--xml")
        command.add("$dataFileName.xml")

        log.info("Runs command: ${command.joinToString(" ")}")

        CommandLineRunner.run(command as ArrayList<String>)

        return testExecutionError
    }

    /**
     * Saves data of a given test case to a report.
     *
     * @param testCaseName The test case name.
     * @param testCaseCode The test case code.
     * @param xmlFileName The XML file name to read data from.
     */
    private fun getTestCaseFromXml(
        testCaseId: Int,
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
                        isCorrectSourceFile = this.attributes.getValue("name") == project.service<ProjectContextService>().fileUrl!!.split(File.separatorChar).last()
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

        return TestCase(testCaseId, testCaseName, testCaseCode, setOfLines, setOf(), setOf())
    }

    /**
     * Check for exception and collect lines covered during the exception happening.
     *
     * @param testExecutionError error output (including the thrown stack trace) during the test execution.
     * @return a set of lines that are covered in CUT during the exception happening.
     */
    private fun getExceptionData(testExecutionError: String): Pair<Boolean, Set<Int>> {
        if (testExecutionError.isBlank()) {
            return Pair(false, emptySet())
        }

        val result = mutableSetOf<Int>()

        // get frames
        val frames = testExecutionError.split("\n\tat ").toMutableList()
        frames.removeFirst()

        frames.forEach { frame ->
            if (frame.contains(project.service<ProjectContextService>().classFQN!!)) {
                val coveredLineNumber = frame.split(":")[1].replace(")", "").toIntOrNull()
                if (coveredLineNumber != null) {
                    result.add(coveredLineNumber)
                }
            }
        }

        return Pair(
            Regex("(^\\d+\\) .+)|(^.+(Exception|Error): .+)|(^\\s+at .+)|(^\\s+... \\d+ more)|(^\\s*Caused by:.+)").find(
                testExecutionError,
            ) != null,
            result,
        )
    }

    /**
     * Update the code of the test.
     *
     * @param fileName new tmp filename
     * @param testId new id of test
     * @param testCode new code of test
     * @param testName the name of the test
     */
    fun processNewTestCase(fileName: String, testId: Int, testName: String, testCode: String): TestCase {
        // get buildPath
        var buildPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        if (project.service<SettingsProjectService>().state.buildPath.isEmpty()) {
            // User did not set own path
            buildPath = getBuildPath(project)
        }

        // save new test to file
        val generatedTestPath: String = saveGeneratedTest(
            project.service<TestGenerationDataService>().packageLine,
            testCode,
            project.service<ProjectContextService>().resultPath!!,
            fileName,
        )

        // compilation checking
        val compilationResult = compileCode(generatedTestPath, buildPath)
        if (!compilationResult.first) {
            project.service<TestsExecutionResultService>().addFailedTest(testId, testCode, compilationResult.second)
        } else {
            val dataFileName = "${project.service<ProjectContextService>().resultPath!!}/jacoco-${fileName.split(".")[0]}"

            val testExecutionError = createXmlFromJacoco(
                fileName.split(".")[0],
                dataFileName,
                testName,
                buildPath,
                project.service<TestGenerationDataService>().packageLine,
            )

            if (!File("$dataFileName.xml").exists()) {
                project.service<TestsExecutionResultService>().addFailedTest(testId, testCode, testExecutionError)
            } else {
                val testCase = getTestCaseFromXml(
                    testId,
                    testName,
                    testCode,
                    getExceptionData(testExecutionError).second,
                    "$dataFileName.xml",
                )

                if (getExceptionData(testExecutionError).first) {
                    project.service<TestsExecutionResultService>().addFailedTest(testId, testCode, testExecutionError)
                } else {
                    project.service<TestsExecutionResultService>().addPassedTest(testId, testCode)
                }

                DataFilesUtil.cleanFolder(project.service<ProjectContextService>().resultPath!!)

                return testCase
            }
        }
        DataFilesUtil.cleanFolder(project.service<ProjectContextService>().resultPath!!)

        return TestCase(testId, testName, testCode, setOf(), setOf(), setOf())
    }
}
