package org.jetbrains.research.testspark.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testspark.TestSparkBundle
import org.jetbrains.research.testspark.Util
import org.jetbrains.research.testspark.actions.getSurroundingClass
import org.jetbrains.research.testspark.data.FragmentToTestDada
import org.jetbrains.research.testspark.editor.Workspace
import org.jetbrains.research.testspark.services.CommandLineService
import org.jetbrains.research.testspark.tools.template.generation.ProcessManager

/**
 * Pipeline class represents a pipeline for running the test generation process.
 *
 * @param e The AnActionEvent instance that triggered the pipeline.
 * @param packageName The name of the package where the target class resides.
 */
class Pipeline(
    e: AnActionEvent,
    private val packageName: String,
) {
    private val project = e.project!!

    init {
        project.service<Workspace>().projectClassPath = ProjectRootManager.getInstance(project).contentRoots.first().path

        project.service<Workspace>().testResultDirectory = project.service<CommandLineService>().testResultDirectory
        project.service<Workspace>().testResultName = project.service<CommandLineService>().testResultName
        project.service<Workspace>().resultPath = project.service<CommandLineService>().resultPath

        project.service<Workspace>().baseDir = "${project.service<Workspace>().testResultDirectory}${project.service<Workspace>().testResultName}-validation"

        project.service<Workspace>().serializeResultPath = "\"${project.service<Workspace>().testResultDirectory}${project.service<Workspace>().testResultName}\""

        project.service<Workspace>().vFile = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)!!
        project.service<Workspace>().fileUrl = project.service<Workspace>().vFile!!.presentableUrl
        project.service<Workspace>().modificationStamp = project.service<Workspace>().vFile!!.modificationStamp

        project.service<Workspace>().cutPsiClass = getSurroundingClass(
            e.dataContext.getData(CommonDataKeys.PSI_FILE)!!,
            e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!,
        )
        project.service<Workspace>().cutModule = ProjectFileIndex.getInstance(project).getModuleForFile(project.service<Workspace>().cutPsiClass!!.containingFile.virtualFile)!!

        project.service<Workspace>().classFQN = project.service<Workspace>().cutPsiClass!!.qualifiedName!!
        project.service<Workspace>().key = getKey(project, project.service<Workspace>().classFQN!!)

        Util.makeTmp()
        Util.makeDir(project.service<Workspace>().baseDir!!)
    }

    /**
     * Builds the project and launches generation on a separate thread.
     */
    fun runTestGeneration(processManager: ProcessManager, codeType: FragmentToTestDada) {
        clearDataBeforeTestGeneration(project, project.service<Workspace>().testResultName!!)

        val projectBuilder = ProjectBuilder(project)

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestSparkBundle.message("testGenerationMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    if (processStopped(project, indicator)) return

                    if (projectBuilder.runBuild(indicator)) {
                        if (processStopped(project, indicator)) return

                        processManager.runTestGenerator(
                            indicator,
                            codeType,
                            packageName,
                        )
                    }

                    if (processStopped(project, indicator)) return

                    indicator.stop()
                }
            })
    }
}
