package nl.tudelft.ewi.se.ciselab.testgenie.listener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.TestGenerationResultListener
import nl.tudelft.ewi.se.ciselab.testgenie.services.CoverageVisualisationService
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestCaseDisplayService
import org.evosuite.utils.CompactReport

class TestGenerationResultListenerImpl(private val project: Project) : TestGenerationResultListener {
    private val log = Logger.getInstance(this.javaClass)

    override fun testGenerationResult(testReport: CompactReport) {
        log.info("Received test result for " + testReport.UUT)

        val testCaseDisplayService = project.service<TestCaseDisplayService>()

        ApplicationManager.getApplication().invokeLater {
            testCaseDisplayService.displayTestCases(testReport)
        }

        val coverageVisualisationService = project.service<CoverageVisualisationService>()

        ApplicationManager.getApplication().invokeLater {
            coverageVisualisationService.showCoverage(testReport)
        }
    }
}
