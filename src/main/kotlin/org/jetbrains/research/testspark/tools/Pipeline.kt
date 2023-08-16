package org.jetbrains.research.testspark.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
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

    private val projectClassPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path

    private val testResultDirectory = project.service<CommandLineService>().testResultDirectory
    private val testResultName = project.service<CommandLineService>().testResultName
    private val resultPath = project.service<CommandLineService>().resultPath

    private var baseDir = "$testResultDirectory$testResultName-validation"

    private val serializeResultPath = "\"$testResultDirectory$testResultName\""

    private val vFile = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)!!
    private val fileUrl = vFile.presentableUrl
    private val modificationStamp = vFile.modificationStamp

    private val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
    private val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
    private val cutPsiClass: PsiClass = getSurroundingClass(psiFile, caret)
    private val cutModule: Module = ProjectFileIndex.getInstance(project).getModuleForFile(cutPsiClass.containingFile.virtualFile)!!

    private val classFQN = cutPsiClass.qualifiedName!!

    init {
        Util.makeTmp()
        Util.makeDir(baseDir)

        project.service<Workspace>().key = getKey(fileUrl, classFQN, modificationStamp, testResultName, projectClassPath)
    }

    /**
     * Builds the project and launches generation on a separate thread.
     */
    fun runTestGeneration(processManager: ProcessManager, codeType: FragmentToTestDada) {
        clearDataBeforeTestGeneration(project, testResultName)

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
                            projectClassPath,
                            resultPath,
                            serializeResultPath,
                            packageName,
                            cutModule,
                            classFQN,
                            fileUrl,
                            testResultName,
                            baseDir,
                            modificationStamp,
                        )
                    }

                    if (processStopped(project, indicator)) return

                    indicator.stop()
                }
            })
    }
}
