package nl.tudelft.ewi.se.ciselab.testgenie.listener

import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.TestGenerationResultListener
import nl.tudelft.ewi.se.ciselab.testgenie.services.CoverageVisualisationService
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestCaseDisplayService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.evosuite.utils.CompactReport

class TestGenerationResultListenerImpl(private val project: Project) : TestGenerationResultListener {
    private val log = Logger.getInstance(this.javaClass)

    override fun testGenerationResult(testReport: CompactReport) {
        log.info("Received test result for " + testReport.UUT)

        val testCaseDisplayService = project.service<TestCaseDisplayService>()

        val tests = testReport.testCaseList.values.map { it.testCode }
        ApplicationManager.getApplication().invokeLater {
            testCaseDisplayService.displayTestCases(tests)
        }

        val coverageVisualisationService = project.service<CoverageVisualisationService>()

        ApplicationManager.getApplication().invokeLater {
            coverageVisualisationService.showCoverage(testReport)
        }
    }
}