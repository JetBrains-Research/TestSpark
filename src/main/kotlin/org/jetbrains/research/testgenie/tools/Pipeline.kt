package org.jetbrains.research.testgenie.tools

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.Util
import org.jetbrains.research.testgenie.data.CodeTypeAndAdditionData
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.tools.template.generation.ProcessManager
import java.io.File
import java.util.UUID

class Pipeline(
    private val project: Project,
    projectClassPath: String,
    private val cutModule: Module,
    private val packageName: String,
    modificationStamp: Long,
    private val fileUrl: String,
    private val classFQN: String,
) {
    private val sep = File.separatorChar

    private val log = Logger.getInstance(this::class.java)

    private val id = UUID.randomUUID().toString()
    private val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"
    private val testResultName = "test_gen_result_$id"
    private var baseDir = "$testResultDirectory$testResultName-validation"

    private val serializeResultPath = "\"$testResultDirectory$testResultName\""

    private val resultPath = "$testResultDirectory$testResultName"

    init {
        Util.makeTmp()
        Util.makeDir(baseDir)

        project.service<Workspace>().key = getKey(fileUrl, classFQN, modificationStamp, testResultName, projectClassPath)
    }

    /**
     * Builds the project and launches generation on a separate thread.
     */
    fun runTestGeneration(processManager: ProcessManager, codeType: CodeTypeAndAdditionData) {
        clearDataBeforeTestGeneration(project, testResultName)

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
                            serializeResultPath,
                            packageName,
                            cutModule,
                            classFQN,
                            fileUrl,
                            testResultName,
                            baseDir,
                            log,
                        )
                    }

                    indicator.stop()
                }
            })
    }
}
