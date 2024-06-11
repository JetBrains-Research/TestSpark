package org.jetbrains.research.testspark.tools

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.research.testspark.core.data.TestCase
import org.jetbrains.research.testspark.core.test.TestsPersistentStorage
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import org.jetbrains.research.testspark.data.ProjectContext
import org.jetbrains.research.testspark.core.ProjectUnderTestFileCreator
import org.jetbrains.research.testspark.services.LLMSettingsService
import org.jetbrains.research.testspark.services.PluginSettingsService
import org.jetbrains.research.testspark.services.TestsExecutionResultService
import org.jetbrains.research.testspark.settings.llm.LLMSettingsState
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

class TestProcessor(
    val project: Project,
    givenProjectSDK: Sdk? = null
) : TestsPersistentStorage {
    private val projectSDK: Sdk = givenProjectSDK ?: ProjectRootManager.getInstance(project).projectSdk!!
    private val javaHomeDirectory = projectSDK.homeDirectory!!

    private val log = Logger.getInstance(this.javaClass)

    private val llmSettingsState: LLMSettingsState
        get() = project.getService(LLMSettingsService::class.java).state

    val testCompiler = TestCompilerFactory.createJavacTestCompiler(project, llmSettingsState.junitVersion, javaHomeDirectory.path)

    override fun saveGeneratedTest(packageString: String, code: String, resultPath: String, testFileName: String): String {
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
        resultPath: String,
        projectContext: ProjectContext,
    ): String {
        // find the proper javac
        val javaRunner = File(javaHomeDirectory.path).walk()
            .filter {
                val isJavaName = if (DataFilesUtil.isWindows()) it.name.equals("java.exe") else it.name.equals("java")
                isJavaName && it.isFile
            }
            .first()
        // JaCoCo libs
        val jacocoAgentLibraryPath = "\"${LibraryPathsProvider.getJacocoAgentLibraryPath()}\""
        val jacocoCLILibraryPath = "\"${LibraryPathsProvider.getJacocoCliLibraryPath()}\""

        val sourceRoots = ModuleRootManager.getInstance(projectContext.cutModule!!).getSourceRoots(false)

        // unique name
        var name = if (generatedTestPackage.isEmpty()) "" else "$generatedTestPackage."
        name += "$className#$testCaseName"

        val junitVersion = llmSettingsState.junitVersion.version

        // run the test method with jacoco agent
        val junitRunnerLibraryPath = LibraryPathsProvider.getJUnitRunnerLibraryPath()
        val testExecutionError = CommandLineRunner.run(
            arrayListOf(
                javaRunner.absolutePath,
                "-javaagent:$jacocoAgentLibraryPath=destfile=$dataFileName.exec,append=false,includes=${projectContext.classFQN}",
                "-cp",
                "\"${testCompiler.getPath(projectBuildPath)}${DataFilesUtil.classpathSeparator}${junitRunnerLibraryPath}${DataFilesUtil.classpathSeparator}$resultPath\"",
                "org.jetbrains.research.SingleJUnitTestRunner$junitVersion",
                name,
            ),
        )

        log.info("Test execution error message: $testExecutionError")

        // Prepare the command for generating the Jacoco report
        val command = mutableListOf(
            javaRunner.absolutePath,
            "-jar",
            // jacocoCLIDir,
            jacocoCLILibraryPath,
            "report",
            "$dataFileName.exec",
        )

        // for classpath containing cut
        command.add("--classfiles")
        command.add("${projectContext.compilationOutputDirectory!!}")
        // command.add(CompilerModuleExtension.getInstance(projectContext.cutModule!!)?.compilerOutputPath!!.path)

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
     * Update the code of the test.
     *
     * @param fileName new tmp filename
     * @param testId new id of test
     * @param testCode new code of test
     * @param testName the name of the test
     */
    fun processNewTestCase(
        fileName: String,
        testId: Int,
        testName: String,
        testCode: String,
        packageLine: String,
        resultPath: String,
        projectContext: ProjectContext,
    ): TestCase {
        // get buildPath
        var buildPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        if (project.service<PluginSettingsService>().state.buildPath.isEmpty()) {
            // User did not set own path
            buildPath = ToolUtils.getBuildPath(project)
        }

        // save new test to file
        val generatedTestPath: String = saveGeneratedTest(
            packageLine,
            testCode,
            resultPath,
            fileName,
        )

        // compilation checking
        val compilationResult = testCompiler.compileCode(generatedTestPath, buildPath)
        if (!compilationResult.first) {
            project.service<TestsExecutionResultService>().addFailedTest(testId, testCode, compilationResult.second)
        } else {
            val dataFileName = "$resultPath/jacoco-${fileName.split(".")[0]}"

            val testExecutionError = createXmlFromJacoco(
                fileName.split(".")[0],
                dataFileName,
                testName,
                buildPath,
                packageLine,
                resultPath,
                projectContext,
            )

            if (!File("$dataFileName.xml").exists()) {
                project.service<TestsExecutionResultService>().addFailedTest(testId, testCode, testExecutionError)
            } else {
                val testCase = getTestCaseFromXml(
                    testId,
                    testName,
                    testCode,
                    getExceptionData(testExecutionError, projectContext).second,
                    "$dataFileName.xml",
                    projectContext,
                )

                if (getExceptionData(testExecutionError, projectContext).first) {
                    project.service<TestsExecutionResultService>().addFailedTest(testId, testCode, testExecutionError)
                } else {
                    project.service<TestsExecutionResultService>().addPassedTest(testId, testCode)
                }

                DataFilesUtil.cleanFolder(resultPath)

                return testCase
            }
        }
        DataFilesUtil.cleanFolder(resultPath)

        return TestCase(testId, testName, testCode, setOf())
    }

    /**
     * Check for exception and collect lines covered during the exception happening.
     *
     * @param testExecutionError error output (including the thrown stack trace) during the test execution.
     * @return a set of lines that are covered in CUT during the exception happening.
     */
    private fun getExceptionData(testExecutionError: String, projectContext: ProjectContext): Pair<Boolean, Set<Int>> {
        if (testExecutionError.isBlank()) {
            return Pair(false, emptySet())
        }

        val result = mutableSetOf<Int>()

        // get frames
        val frames = testExecutionError.split("\n\tat ").toMutableList()
        frames.removeFirst()

        frames.forEach { frame ->
            if (frame.contains(projectContext.classFQN!!)) {
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
        projectContext: ProjectContext,
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
                        isCorrectSourceFile = this.attributes.getValue("name") == projectContext.fileUrlAsString!!.split(File.separatorChar).last()
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

        return TestCase(testCaseId, testCaseName, testCaseCode, setOfLines)
    }
}
