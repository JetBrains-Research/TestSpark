package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.fixtures.*
import org.assertj.core.api.Assertions.assertThat
import org.evosuite.result.BranchInfo
import org.evosuite.result.MutationInfo
import org.evosuite.result.TestGenerationResultImpl
import org.evosuite.shaded.org.mockito.Mockito
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
        val branch1 = Mockito.mock(BranchInfo::class.java)
        val branch2 = Mockito.mock(BranchInfo::class.java)
        val branch3 = Mockito.mock(BranchInfo::class.java)
        val branch4 = Mockito.mock(BranchInfo::class.java)
        val branch5 = Mockito.mock(BranchInfo::class.java)

        compactReport = CompactReport(TestGenerationResultImpl())
        compactReport.UUT = "MyClass"
        compactReport.allCoveredLines = setOf(1, 2, 5, 7)
        compactReport.allUncoveredLines = setOf(3, 4, 6)
        compactReport.allUncoveredBranches = setOf(branch1, branch2, branch3)
        compactReport.allCoveredBranches = setOf(branch4, branch5)
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

    @Test
    fun fillToolWindowContents() {
        coverageVisualisationService.showCoverage(compactReport)
        assertThat(coverageToolWindowDisplayService.data[0]).isEqualTo("MyClass")
        assertThat(coverageToolWindowDisplayService.data[1]).isEqualTo("57% (4/7)")
        assertThat(coverageToolWindowDisplayService.data[2]).isEqualTo("40% (2/5)")
        assertThat(coverageToolWindowDisplayService.data[3]).isEqualTo("100% (1/1)")
    }
}
