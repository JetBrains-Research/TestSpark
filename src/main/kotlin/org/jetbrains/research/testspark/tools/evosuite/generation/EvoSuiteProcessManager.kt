package org.jetbrains.research.testspark.tools.evosuite.generation

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import org.evosuite.utils.CompactReport
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.services.ProjectContextService
import org.jetbrains.research.testspark.services.RunCommandLineService
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.services.SettingsProjectService
import org.jetbrains.research.testspark.tools.evosuite.SettingsArguments
import org.jetbrains.research.testspark.tools.evosuite.error.EvoSuiteErrorManager
import org.jetbrains.research.testspark.tools.getBuildPath
import org.jetbrains.research.testspark.tools.getImportsCodeFromTestSuiteCode
import org.jetbrains.research.testspark.tools.getPackageFromTestSuiteCode
import org.jetbrains.research.testspark.tools.processStopped
import org.jetbrains.research.testspark.tools.saveData
import org.jetbrains.research.testspark.tools.template.generation.ProcessManager
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset
import java.util.regex.Pattern
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

/**
 * This class manages the execution of EvoSuite, a test generation tool.
 *
 * @param project the project in which the tests will be generated
 * @param projectPath the path to the project directory
 */
class EvoSuiteProcessManager(
    private val project: Project,
    private val projectPath: String,
) : ProcessManager {
    private val log = Logger.getInstance(this::class.java)

    private val evoSuiteProcessTimeout: Long = 12000000 // TODO: Source from config
    private val evosuiteVersion = "1.0.5" // TODO: Figure out a better way to source this

    private val sep = File.separatorChar
    private val pluginsPath = com.intellij.openapi.application.PathManager.getPluginsPath()
    private var evoSuitePath = "$pluginsPath${sep}TestSpark${sep}lib${sep}evosuite-$evosuiteVersion.jar"

    private val settingsApplicationState = SettingsApplicationService.getInstance().state!!
    private val settingsProjectState = project.service<SettingsProjectService>().state

    private val evoSuiteErrorManager: EvoSuiteErrorManager = EvoSuiteErrorManager()

    /**
     * Executes EvoSuite.
     *
     * @param indicator the progress indicator
     */
    override fun runTestGenerator(
        indicator: ProgressIndicator,
        codeType: FragmentToTestData,
        packageName: String,
    ) {
        try {
            if (processStopped(project, indicator)) return

            val regex = Regex("version \"(.*?)\"")
            val version = regex.find(project.service<RunCommandLineService>().runCommandLine(arrayListOf(settingsApplicationState.javaPath, "-version")))
                ?.groupValues
                ?.get(1)
                ?.split(".")
                ?.get(0)
                ?.toInt()

            if (version == null || version > 11) {
                evoSuiteErrorManager.errorProcess(TestSparkBundle.message("incorrectJavaVersion"), project)
                return
            }

            val projectClassPath = project.service<ProjectContextService>().projectClassPath!!
            val classFQN = project.service<ProjectContextService>().cutPsiClass!!.qualifiedName!!
            val baseDir = project.service<ProjectContextService>().baseDir!!
            val resultName = "${project.service<ProjectContextService>().resultPath}${sep}EvoSuiteResult"

            Path(project.service<ProjectContextService>().resultPath!!).createDirectories()

            // get command
            val command = when (codeType.type!!) {
                CodeType.CLASS -> SettingsArguments(projectClassPath, projectPath, resultName, classFQN, baseDir).build()
                CodeType.METHOD -> {
                    SettingsArguments(projectClassPath, projectPath, resultName, classFQN, baseDir).forMethod(codeType.objectDescription).build()
                }

                CodeType.LINE -> SettingsArguments(projectClassPath, projectPath, resultName, classFQN, baseDir).forLine(codeType.objectIndex).build(true)
            }

            if (settingsApplicationState.seed.isNotBlank()) command.add("-seed=${settingsApplicationState.seed}")
            if (settingsApplicationState.configurationId.isNotBlank()) command.add("-Dconfiguration_id=${settingsApplicationState.configurationId}")

            // update build path
            var buildPath = projectClassPath
            if (settingsProjectState.buildPath.isEmpty()) {
                // User did not set own path
                buildPath = getBuildPath(project)
            }
            command[command.indexOf(projectClassPath)] = buildPath
            log.info("Generating tests for project $projectPath with classpath $buildPath inside the project")

            // construct command
            val cmd = ArrayList<String>()
            cmd.add(settingsApplicationState.javaPath)
            cmd.add("-Djdk.attach.allowAttachSelf=true")
            cmd.add("-jar")
            cmd.add(evoSuitePath)
            cmd.addAll(command)

            val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }
            log.info("Starting EvoSuite with arguments: $cmdString")

//            indicator.isIndeterminate = false
            indicator.text = TestSparkBundle.message("searchMessage")
            val evoSuiteProcess = GeneralCommandLine(cmd)
            evoSuiteProcess.charset = Charset.forName("UTF-8")
            evoSuiteProcess.setWorkDirectory(projectPath)
            val handler = OSProcessHandler(evoSuiteProcess)

            // attach process listener for output
            handler.addProcessListener(object : ProcessAdapter() {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (processStopped(project, indicator)) {
                        handler.destroyProcess()
                        return
                    }

                    val text = event.text

                    evoSuiteErrorManager.addLineToEvoSuiteOutput(text)

                    val progressMatcher =
                        Pattern.compile("Progress:[>= ]*(\\d+(?:\\.\\d+)?)%").matcher(text)
                    val coverageMatcher = Pattern.compile("Cov:[>= ]*(\\d+(?:\\.\\d+)?)%").matcher(text)

                    log.info(text) // kept for debugging purposes

                    val progress =
                        if (progressMatcher.find()) {
                            progressMatcher.group(1)?.toDouble()?.div(100)
                        } else {
                            null
                        }
                    val coverage =
                        if (coverageMatcher.find()) {
                            coverageMatcher.group(1)?.toDouble()?.div(100)
                        } else {
                            null
                        }

                    if (progress != null && coverage != null) {
                        indicator.fraction = if (progress >= coverage) progress else coverage
                    } else if (progress != null) {
                        indicator.fraction = progress
                    } else if (coverage != null) {
                        indicator.fraction = coverage
                    }

                    if (indicator.fraction == 1.0 && indicator.text != TestSparkBundle.message("testCasesSaving")) {
                        indicator.text = TestSparkBundle.message("testCasesSaving")
                    }
                }
            })

            handler.startNotify()

            if (processStopped(project, indicator)) return

            // evosuite errors check
            if (!evoSuiteErrorManager.isProcessCorrect(handler, project, evoSuiteProcessTimeout, indicator)) return

            val gson = Gson()
            val reader = JsonReader(FileReader(resultName))

            val testGenerationResult: CompactReport = gson.fromJson(reader, CompactReport::class.java)

            saveData(
                project,
                Report(testGenerationResult),
                getPackageFromTestSuiteCode(testGenerationResult.testSuiteCode),
                getImportsCodeFromTestSuiteCode(testGenerationResult.testSuiteCode, classFQN),
                indicator,
            )
        } catch (e: Exception) {
            evoSuiteErrorManager.errorProcess(TestSparkBundle.message("evosuiteErrorMessage").format(e.message), project)
            e.printStackTrace()
        }
    }
}
