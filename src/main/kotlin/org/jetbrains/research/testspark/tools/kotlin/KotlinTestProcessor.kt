package org.jetbrains.research.testspark.tools.kotlin

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testspark.core.data.TestCase
import org.jetbrains.research.testspark.core.test.TestsPersistentStorage
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import org.jetbrains.research.testspark.core.utils.Language
import org.jetbrains.research.testspark.data.ProjectContext
import org.jetbrains.research.testspark.services.LLMSettingsService
import org.jetbrains.research.testspark.services.PluginSettingsService
import org.jetbrains.research.testspark.services.TestsExecutionResultService
import org.jetbrains.research.testspark.settings.llm.LLMSettingsState
import org.jetbrains.research.testspark.tools.LibraryPathsProvider
import org.jetbrains.research.testspark.tools.TestCompilerFactory
import org.jetbrains.research.testspark.tools.ToolUtils
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

class KotlinTestProcessor(
    val project: Project,
    givenProjectSDKPath: Path? = null
) : TestsPersistentStorage {
    private val kotlinHomeDirectory =
        givenProjectSDKPath?.toString() ?: ProjectRootManager.getInstance(project).projectSdk!!.homeDirectory!!.path

    private val log = Logger.getInstance(this::class.java)

    private val llmSettingsState: LLMSettingsState
        get() = project.getService(LLMSettingsService::class.java).state

    override val testCompiler =
        TestCompilerFactory.createJavacTestCompiler(project, llmSettingsState.junitVersion, kotlinHomeDirectory, Language.Kotlin)

    override fun saveGeneratedTest(
        packageString: String,
        code: String,
        resultPath: String,
        testFileName: String
    ): String {
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

    fun createXmlFromJacoco(
        className: String,
        dataFileName: String,
        testCaseName: String,
        projectBuildPath: String,
        generatedTestPackage: String,
        resultPath: String,
        projectContext: ProjectContext
    ): String {
        val javaRunner = File(kotlinHomeDirectory).walk()
            .filter {
                val isKotlinName = if (DataFilesUtil.isWindows()) it.name.equals("kotlin.exe") else it.name.equals("kotlin")
                isKotlinName && it.isFile
            }
            .first()

        val jacocoAgentLibraryPath = "\"${LibraryPathsProvider.getJacocoAgentLibraryPath()}\""
        val jacocoCLILibraryPath = "\"${LibraryPathsProvider.getJacocoCliLibraryPath()}\""
        val sourceRoots = ModuleRootManager.getInstance(projectContext.cutModule!!).getSourceRoots(false)

        var name = if (generatedTestPackage.isEmpty()) "" else "$generatedTestPackage."
        name += "$className#$testCaseName"

        val junitVersion = llmSettingsState.junitVersion.version

        val junitRunnerLibraryPath = LibraryPathsProvider.getJUnitRunnerLibraryPath()
        val testExecutionError = CommandLineRunner.run(
            arrayListOf(
                javaRunner.absolutePath,
                "-javaagent:$jacocoAgentLibraryPath=destfile=$dataFileName.exec,append=false,includes=${projectContext.classFQN}",
                "-cp",
                "\"${testCompiler.getPath(projectBuildPath)}${DataFilesUtil.classpathSeparator}${junitRunnerLibraryPath}${DataFilesUtil.classpathSeparator}$resultPath\"",
                "org.jetbrains.research.SingleJUnitTestRunner$junitVersion",
                name,
            )
        )

        log.info("Test execution error message: $testExecutionError")

        val command = mutableListOf(
            javaRunner.absolutePath,
            "-jar",
            jacocoCLILibraryPath,
            "report",
            "$dataFileName.exec",
        )

        command.add("--classfiles")
        command.add(CompilerModuleExtension.getInstance(projectContext.cutModule!!)?.compilerOutputPath!!.path)

        sourceRoots.forEach { root ->
            command.add("--sourcefiles")
            command.add(root.path)
        }

        command.add("--xml")
        command.add("$dataFileName.xml")

        log.info("Runs command: ${command.joinToString(" ")}")

        CommandLineRunner.run(command as ArrayList<String>)

        return testExecutionError
    }

    fun processNewTestCase(
        fileName: String,
        testId: Int,
        testName: String,
        testCode: String,
        packageLine: String,
        resultPath: String,
        projectContext: ProjectContext
    ): TestCase {
        var buildPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        if (project.service<PluginSettingsService>().state.buildPath.isEmpty()) {
            buildPath = ToolUtils.getBuildPath(project)
        }

        val generatedTestPath: String = saveGeneratedTest(
            packageLine,
            testCode,
            resultPath,
            fileName,
        )

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

    private fun getExceptionData(testExecutionError: String, projectContext: ProjectContext): Pair<Boolean, Set<Int>> {
        if (testExecutionError.isBlank()) {
            return Pair(false, emptySet())
        }

        val result = mutableSetOf<Int>()
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
                        isCorrectSourceFile =
                            this.attributes.getValue("name") == projectContext.fileUrlAsString!!.split(File.separatorChar)
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

        setOfLines.addAll(linesCoveredDuringTheException)

        return TestCase(testCaseId, testCaseName, testCaseCode, setOfLines)
    }
}
