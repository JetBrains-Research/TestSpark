package org.jetbrains.research.testspark.tools.evosuite.generation

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import org.evosuite.utils.CompactReport
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.utils.CommandLineRunner
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.data.IJReport
import org.jetbrains.research.testspark.data.ProjectContext
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.services.SettingsProjectService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import org.jetbrains.research.testspark.tools.evosuite.SettingsArguments
import org.jetbrains.research.testspark.tools.evosuite.error.EvoSuiteErrorManager
import org.jetbrains.research.testspark.tools.getBuildPath
import org.jetbrains.research.testspark.tools.getImportsCodeFromTestSuiteCode
import org.jetbrains.research.testspark.tools.getPackageFromTestSuiteCode
import org.jetbrains.research.testspark.tools.llm.generation.StandardRequestManagerFactory
import org.jetbrains.research.testspark.tools.processStopped
import org.jetbrains.research.testspark.tools.saveData
import org.jetbrains.research.testspark.tools.sep
import org.jetbrains.research.testspark.tools.template.generation.ProcessManager
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

    private val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

    private val evoSuiteProcessTimeout: Long = 12000000 // TODO: Source from config
    private val evosuiteVersion = "1.0.5" // TODO: Figure out a better way to source this

    private val pluginsPath = com.intellij.openapi.application.PathManager.getPluginsPath()
    private var evoSuitePath = "$pluginsPath${sep}TestSpark${sep}lib${sep}evosuite-$evosuiteVersion.jar"

    private val settingsProjectState = project.service<SettingsProjectService>().state

    private val evoSuiteErrorManager: EvoSuiteErrorManager = EvoSuiteErrorManager()

    /**
     * Executes EvoSuite.
     *
     * @param indicator the progress indicator
     */
    override fun runTestGenerator(
        indicator: CustomProgressIndicator,
        codeType: FragmentToTestData,
        packageName: String,
        projectContext: ProjectContext,
        generatedTestData: TestGenerationData,
    ): UIContext? {
        try {
            if (processStopped(project, indicator)) return null

            val regex = Regex("version \"(.*?)\"")
            val version = regex.find(CommandLineRunner.run(arrayListOf(settingsState.javaPath, "-version")))
                ?.groupValues
                ?.get(1)
                ?.split(".")
                ?.get(0)
                ?.toInt()

            if (version == null || version > 11) {
                evoSuiteErrorManager.errorProcess(TestSparkBundle.message("incorrectJavaVersion"), project)
                return null
            }

            val projectClassPath = projectContext.projectClassPath!!
            val classFQN = projectContext.classFQN!!
            val baseDir = generatedTestData.baseDir!!
            val resultName = "${generatedTestData.resultPath}${sep}EvoSuiteResult"

            Path(generatedTestData.resultPath).createDirectories()

            // get command
            val command = when (codeType.type!!) {
                CodeType.CLASS -> SettingsArguments(projectClassPath, projectPath, resultName, classFQN, baseDir).build()
                CodeType.METHOD -> {
                    SettingsArguments(projectClassPath, projectPath, resultName, classFQN, baseDir).forMethod(codeType.objectDescription).build()
                }

                CodeType.LINE -> SettingsArguments(projectClassPath, projectPath, resultName, classFQN, baseDir).forLine(codeType.objectIndex).build(true)
            }

            if (settingsState.seed.isNotBlank()) command.add("-seed=${settingsState.seed}")
            if (settingsState.configurationId.isNotBlank()) command.add("-Dconfiguration_id=${settingsState.configurationId}")
            if (settingsState.evosuitePort.isNotBlank()) command.add("-Dprocess_communication_port=${settingsState.evosuitePort}")

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
            cmd.add(settingsState.javaPath)
            cmd.add("-Djdk.attach.allowAttachSelf=true")
            cmd.add("-jar")
            cmd.add(evoSuitePath)
            cmd.addAll(command)

            val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }
            log.info("Starting EvoSuite with arguments: $cmdString")

//            indicator.isIndeterminate = false
            indicator.setText(TestSparkBundle.message("searchMessage"))

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
                        indicator.setFraction(if (progress >= coverage) progress else coverage)
                    } else if (progress != null) {
                        indicator.setFraction(progress)
                    } else if (coverage != null) {
                        indicator.setFraction(coverage)
                    }

                    if (indicator.getFraction() == 1.0 && indicator.getText() != TestSparkBundle.message("testCasesSaving")) {
                        indicator.setText(TestSparkBundle.message("testCasesSaving"))
                    }
                }
            })

            handler.startNotify()

            if (processStopped(project, indicator)) return null

            // evosuite errors check
            if (!evoSuiteErrorManager.isProcessCorrect(handler, project, evoSuiteProcessTimeout, indicator)) return null

            val gson = Gson()
            val reader = JsonReader(FileReader(resultName))

            val testGenerationResult: CompactReport = gson.fromJson(reader, CompactReport::class.java)

            saveData(
                project,
                IJReport(testGenerationResult),
                getPackageFromTestSuiteCode(testGenerationResult.testSuiteCode),
                getImportsCodeFromTestSuiteCode(testGenerationResult.testSuiteCode, classFQN),
                projectContext.fileUrlAsString!!,
                generatedTestData,
            )
        } catch (e: Exception) {
            evoSuiteErrorManager.errorProcess(TestSparkBundle.message("evosuiteErrorMessage").format(e.message), project)
            e.printStackTrace()
        }

        return UIContext(projectContext, generatedTestData, StandardRequestManagerFactory().getRequestManager(project))
    }
}
