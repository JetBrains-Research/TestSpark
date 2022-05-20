package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.fixtures.*
import org.assertj.core.api.Assertions.assertThat
import org.evosuite.result.TestGenerationResultImpl
import org.evosuite.utils.CompactReport
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoverageVisualisationServiceTest {

    private lateinit var fixture: CodeInsightTestFixture
    private lateinit var coverageVisualisationService: CoverageVisualisationService
    private lateinit var compactReport: CompactReport
    private lateinit var project: Project
    private lateinit var coverageToolWindowDisplayService: CoverageToolWindowDisplayService

    @BeforeAll
    fun setUpAll() {
        compactReport = CompactReport(TestGenerationResultImpl())
        compactReport.UUT = "MyClass"
        compactReport.allCoveredLines = setOf(1, 2, 5, 7)
        compactReport.allUncoveredLines = setOf(3, 4, 6)
        compactReport.allUncoveredBranches = setOf(null)
        compactReport.allCoveredBranches = setOf(null)
        compactReport.allCoveredMutation = setOf(null)
        compactReport.allUncoveredMutation = setOf()
    }

    @BeforeEach
    fun setUp() {
        val projectBuilder: TestFixtureBuilder<IdeaProjectTestFixture> =
            IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder("TestGenie")

        fixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.fixture)
        fixture.setUp()

        project = fixture.project
        coverageVisualisationService = project.service()
        coverageToolWindowDisplayService = project.service()

        // Initialise the toolWindow TestGenie
        ToolWindowManager.getInstance(project)
            .registerToolWindow(RegisterToolWindowTask("TestGenie", ToolWindowAnchor.RIGHT))
    }

    
}
