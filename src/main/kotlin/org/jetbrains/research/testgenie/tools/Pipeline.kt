package org.jetbrains.research.testgenie.tools

import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.Util
import org.jetbrains.research.testgenie.services.RunnerService
import org.jetbrains.research.testgenie.data.CodeType
import org.jetbrains.research.testgenie.tools.template.generation.ProcessManager
import java.io.File
import java.util.UUID

class Pipeline(
    private val project: Project,
    projectClassPath: String,
    private val cutModule: Module,
    private val packageName: String,
    modTs: Long,
    private val fileUrl: String,
    private val classFQN: String,
) {
    private val sep = File.separatorChar

    private val id = UUID.randomUUID().toString()
    private val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"
    private val testResultName = "test_gen_result_$id"
    private var baseDir = "$testResultDirectory$testResultName-validation"

    private val resultPath = "$testResultDirectory$testResultName"

    var key = getKey(fileUrl, classFQN, modTs, testResultName, projectClassPath)

    init {
        Util.makeTmp()
        Util.makeDir(baseDir)
    }

    /**
     * Builds the project and launches generation on a separate thread.
     */
    fun runTestGeneration(processManager: ProcessManager, codeType: CodeType) {
        clearDataBeforeTestGeneration(project, key, testResultName)

        val projectBuilder = ProjectBuilder(project)

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestGenieBundle.message("testGenerationMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    if (indicator.isCanceled) {
                        indicator.stop()
                        return
                    }

                    if (projectBuilder.runBuild(indicator)) {
                        processManager.runTestGenerator(
                            indicator,
                            codeType,
                            resultPath,
                            packageName,
                            cutModule,
                            classFQN,
                            fileUrl,
                            testResultName,
                        )
                    }

                    // Revert to previous state
                    val runnerService = project.service<RunnerService>()
                    runnerService.isRunning = false

                    indicator.stop()
                }
            })
    }
}
