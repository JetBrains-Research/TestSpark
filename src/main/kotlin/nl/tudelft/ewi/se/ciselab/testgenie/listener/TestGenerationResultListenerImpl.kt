package nl.tudelft.ewi.se.ciselab.testgenie.listener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import nl.tudelft.ewi.se.ciselab.testgenie.editor.Workspace
import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.TestGenerationResultListener
import nl.tudelft.ewi.se.ciselab.testgenie.services.CoverageVisualisationService
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestCaseDisplayService
import org.evosuite.utils.CompactReport

class TestGenerationResultListenerImpl(private val project: Project) : TestGenerationResultListener {
    private val log = Logger.getInstance(this.javaClass)

    override fun testGenerationResult(testReport: CompactReport, resultName: String) {
        log.info("Received test result for $resultName")
        val workspace = project.service<Workspace>()

        workspace.receiveGenerationResult(resultName, testReport)

        val testCaseDisplayService = project.service<TestCaseDisplayService>()

        ApplicationManager.getApplication().invokeLater {
            testCaseDisplayService.showGeneratedTests(testReport)
        }

        val coverageVisualisationService = project.service<CoverageVisualisationService>()
        ApplicationManager.getApplication().invokeLater {
            coverageVisualisationService.showCoverage(testReport)
        }
    }
}
