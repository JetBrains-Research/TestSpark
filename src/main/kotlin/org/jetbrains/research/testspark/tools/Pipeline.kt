package org.jetbrains.research.testspark.tools

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.data.DataFilesUtil
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.helpers.getSurroundingClass
import org.jetbrains.research.testspark.services.ClearService
import org.jetbrains.research.testspark.services.ProjectContextService
import org.jetbrains.research.testspark.services.TestStorageProcessingService
import org.jetbrains.research.testspark.tools.template.generation.ProcessManager

/**
 * Pipeline class represents a pipeline for generating tests in a project.
 *
 * @param project The project in which the pipeline runs.
 * @param psiFile The current PsiFile object.
 * @param caret The current Caret object.
 * @param fileUrl The URL of the file being processed, if any.
 * @param packageName The package name of the file being processed.
 */
class Pipeline(
    private val project: Project,
    psiFile: PsiFile,
    caret: Caret,
    fileUrl: String?,
    private val packageName: String,
) {
    init {
        project.service<ProjectContextService>().projectClassPath = ProjectRootManager.getInstance(project).contentRoots.first().path
        project.service<ProjectContextService>().resultPath = project.service<TestStorageProcessingService>().resultPath
        project.service<ProjectContextService>().baseDir = "${project.service<TestStorageProcessingService>().testResultDirectory}${project.service<TestStorageProcessingService>().testResultName}-validation"
        project.service<ProjectContextService>().fileUrl = fileUrl

        project.service<ProjectContextService>().cutPsiClass = getSurroundingClass(psiFile, caret)
        project.service<ProjectContextService>().cutModule = ProjectFileIndex.getInstance(project).getModuleForFile(project.service<ProjectContextService>().cutPsiClass!!.containingFile.virtualFile)!!

        project.service<ProjectContextService>().classFQN = project.service<ProjectContextService>().cutPsiClass!!.qualifiedName!!

        DataFilesUtil.makeTmp()
        DataFilesUtil.makeDir(project.service<ProjectContextService>().baseDir!!)
    }

    /**
     * Builds the project and launches generation on a separate thread.
     */
    fun runTestGeneration(processManager: ProcessManager, codeType: FragmentToTestData) {
        project.service<ClearService>().clear(project)

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
