package org.jetbrains.research.testspark.tools

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.data.ProjectContext
import org.jetbrains.research.testspark.data.TestGenerationData
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.display.IJProgressIndicator
import org.jetbrains.research.testspark.helpers.getSurroundingClass
import org.jetbrains.research.testspark.services.CoverageVisualisationService
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.services.ReportLockingService
import org.jetbrains.research.testspark.services.RunnerService
import org.jetbrains.research.testspark.services.TestCaseDisplayService
import org.jetbrains.research.testspark.services.TestsExecutionResultService
import org.jetbrains.research.testspark.tools.template.generation.ProcessManager
import java.io.File
import java.util.*

/**
 * Pipeline class represents a pipeline for generating tests in a project.
 *
 * @param project the project in which the pipeline is executed
 * @param psiFile the PSI file in which the pipeline is executed
 * @param caretOffset the offset of the caret position in the PSI file
 * @param fileUrl the URL of the file being processed, if applicable
 * @param packageName the package name of the file being processed
 */
class Pipeline(
    private val project: Project,
    psiFile: PsiFile,
    caretOffset: Int,
    fileUrl: String?,
    private val packageName: String,
) {
    val projectContext: ProjectContext
    val generatedTestsData = TestGenerationData()

    init {
        val cutPsiClass = getSurroundingClass(psiFile, caretOffset)!!

        // get generated test path
        val sep = File.separatorChar
        val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testSparkResults$sep"
        val id = UUID.randomUUID().toString()
        val testResultName = "test_gen_result_$id"

        projectContext = ProjectContext(
            projectClassPath = ProjectRootManager.getInstance(project).contentRoots.first().path,
            fileUrlAsString = fileUrl,
            cutPsiClass = cutPsiClass,
            classFQN = cutPsiClass.qualifiedName!!,
            cutModule = ProjectFileIndex.getInstance(project).getModuleForFile(cutPsiClass.containingFile.virtualFile)!!,
        )

        generatedTestsData.resultPath = getResultPath(id, testResultDirectory)
        generatedTestsData.baseDir = "${testResultDirectory}$testResultName-validation"
        generatedTestsData.testResultName = testResultName

        DataFilesUtil.makeTmp(FileUtilRt.getTempDirectory())
        DataFilesUtil.makeDir(generatedTestsData.baseDir!!)
    }

    /**
     * Builds the project and launches generation on a separate thread.
     */
    fun runTestGeneration(processManager: ProcessManager, codeType: FragmentToTestData) {
        clear(project)
        val projectBuilder = ProjectBuilder(project)

        var result: UIContext? = null

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestSparkBundle.message("testGenerationMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    val ijIndicator = IJProgressIndicator(indicator)

                    if (processStopped(project, ijIndicator)) return

                    if (projectBuilder.runBuild(ijIndicator)) {
                        if (processStopped(project, ijIndicator)) return

                        result = processManager.runTestGenerator(
                            ijIndicator,
                            codeType,
                            packageName,
                            projectContext,
                            generatedTestsData,
                        )
                    }

                    if (processStopped(project, ijIndicator)) return

                    ijIndicator.stop()
                }

                override fun onFinished() {
                    super.onFinished()
                    project.service<RunnerService>().clear()
                    result?.let {
                        project.service<ReportLockingService>().receiveReport(it)
                    }
                }
            })
    }

    fun clear(project: Project) { // should be removed totally!
        project.service<TestCaseDisplayService>().clear()
        project.service<ErrorService>().clear()
        project.service<CoverageVisualisationService>().clear()
        project.service<TestsExecutionResultService>().clear()
    }
}
