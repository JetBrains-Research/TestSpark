package org.jetbrains.research.testgenie.listener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.evosuite.TestGenerationResultListener
import org.jetbrains.research.testgenie.services.TestCaseCachingService
import org.evosuite.utils.CompactReport

class TestGenerationResultListenerImpl(private val project: Project) : TestGenerationResultListener {
    private val log = Logger.getInstance(this.javaClass)

    override fun testGenerationResult(testReport: CompactReport, resultName: String, fileUrl: String) {
        log.info("Received test result for $resultName")
        val workspace = project.service<Workspace>()

        ApplicationManager.getApplication().invokeLater {
            val jobInfo = workspace.receiveGenerationResult(resultName, testReport)
            cacheGeneratedTestCases(testReport, fileUrl, jobInfo)
        }
    }

    /**
     * Put the generated test cases into the cache.
     * @param testReport the test report
     * @param fileUrl the file url
     * @param jobInfo the job info of the generated tests
     */
    private fun cacheGeneratedTestCases(testReport: CompactReport, fileUrl: String, jobInfo: Workspace.TestJobInfo) {
        val cache = project.service<TestCaseCachingService>()
        cache.putIntoCache(fileUrl, testReport, jobInfo)
    }
}
