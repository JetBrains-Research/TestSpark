package org.jetbrains.research.testspark.editor

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.data.TestGenerationData
import org.jetbrains.research.testspark.services.CoverageVisualisationService
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.services.TestCaseDisplayService
import org.jetbrains.research.testspark.services.TestsExecutionResultService
import java.io.File

/**
 * Workspace state service
 *
 * Handles user workspace state and modifications of that state
 * related to test generation.
 *
 */
@Service(Service.Level.PROJECT)
class Workspace(private val project: Project) {
    data class TestJobInfo(
        val fileUrl: String,
        var targetUnit: String,
        val modificationTS: Long,
        val jobId: String,
        val targetClassPath: String,
    )

    class TestJob(
        val info: TestJobInfo,
        var report: Report,
    ) {
        fun updateReport(report: Report) {
            this.report = report
        }
    }

    var editor: Editor? = null
    var testJob: TestJob? = null

    // The class path of the project.
    var projectClassPath: String? = null
    var testResultDirectory: String? = null
    var testResultName: String? = null

    // The path to save the generated test results.
    var resultPath: String? = null

    // The base directory of the project.
    var baseDir: String? = null
    var vFile: VirtualFile? = null

    // The URL of the file being tested.
    var fileUrl: String? = null

    // The modification stamp of the file being tested.
    var modificationStamp: Long? = null
    var cutPsiClass: PsiClass? = null

    // The module to cut.
    var cutModule: Module? = null

    // The fully qualified name of the class being tested.
    var classFQN: String? = null

    private val log = Logger.getInstance(this.javaClass)

    var testGenerationData = TestGenerationData()

    var key: TestJobInfo? = null

    /**
     * Clears the given project's test-related data, including test case display,
     * error service, coverage visualization, and test generation data.
     *
     * @param project the project to clear the test-related data for
     */
    fun clear(project: Project) {
        project.service<TestCaseDisplayService>().clear()
        project.service<ErrorService>().clear()
        project.service<CoverageVisualisationService>().clear()
        testGenerationData.clear()
        project.service<Workspace>().cleanFolder(resultPath!!)
        project.service<TestsExecutionResultService>().clear()
    }

    /**
     * Updates the state after the action of publishing results.
     *
     * @param testResultName the test result job id which was received
     * @param testReport the generated test suite
     * @param cacheLazyPipeline the runner that was instantiated but not used to create the test suite
     *                        due to a cache hit, or null if there was a cache miss
     * @return the test job that was generated
     */
    fun receiveGenerationResult(
        testResultName: String,
        testReport: Report,
        cachedJobKey: TestJobInfo? = null,
    ): TestJobInfo {
        val pendingJobKey = testGenerationData.pendingTestResults.remove(testResultName)!!

        val jobKey = cachedJobKey ?: pendingJobKey

        val resultsForFile = testGenerationData.testGenerationResults.getOrPut(jobKey.fileUrl) { ArrayList() }

        testJob = TestJob(jobKey, testReport)
        resultsForFile.add(testJob!!)

        updateEditorForFileUrl(jobKey.fileUrl)

        project.service<Workspace>().cleanFolder(resultPath!!)

        if (editor != null) {
            showReport()
        } else {
            log.info("No editor opened for received test result")
        }

        return jobKey
    }

    /**
     * Utility function that returns the editor for a specific file url,
     * in case it is opened in the IDE
     */
    private fun updateEditorForFileUrl(fileUrl: String) {
        val documentManager = FileDocumentManager.getInstance()
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360004480599/comments/360000703299
        FileEditorManager.getInstance(project).selectedEditors.map { it as TextEditor }.map { it.editor }.map {
            val currentFile = documentManager.getFile(it.document)
            if (currentFile != null) {
                if (currentFile.presentableUrl == fileUrl) {
                    editor = it
                }
            }
        }
    }

    fun updateTestCase(testCase: TestCase) {
        val updatedReport = testJob!!.report
        updatedReport.testCaseList.remove(testCase.id)
        updatedReport.testCaseList[testCase.id] = testCase
        updatedReport.normalized()
        testJob!!.updateReport(updatedReport)
        project.service<CoverageVisualisationService>().showCoverage(updatedReport, editor!!)
    }

    /**
     * Function that calls the services responsible for visualizing
     * coverage and displaying the generated test cases. This
     * is used whenever a new test generation result gets published.
     */
    private fun showReport() {
        project.service<TestCaseDisplayService>().showGeneratedTests(editor!!)
        project.service<CoverageVisualisationService>().showCoverage(testJob!!.report, editor!!)
    }

    /**
     * Clean data folder
     */
    fun cleanFolder(path: String) {
        val folder = File(path)

        if (!folder.exists()) return

        if (folder.isDirectory) {
            val files = folder.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        cleanFolder(file.absolutePath)
                    } else {
                        file.delete()
                    }
                }
            }
        }
        folder.delete()
    }
}
