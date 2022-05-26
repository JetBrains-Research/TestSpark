package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.assertj.core.api.Assertions.assertThat
import org.evosuite.result.BranchInfo
import org.evosuite.result.MutationInfo
import org.evosuite.result.TestGenerationResultImpl
import org.evosuite.shaded.org.mockito.Mockito
import org.evosuite.utils.CompactReport
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoverageVisualisationServiceTest : LightJavaCodeInsightFixtureTestCase() {

    private lateinit var coverageVisualisationService: CoverageVisualisationService
    private val myEditor = Mockito.mock(Editor::class.java)
    private lateinit var coverageToolWindowDisplayService: CoverageToolWindowDisplayService

    private val branch1 = Mockito.mock(BranchInfo::class.java)
    private val branch2 = Mockito.mock(BranchInfo::class.java)
    private val branch3 = Mockito.mock(BranchInfo::class.java)
    private val branch4 = Mockito.mock(BranchInfo::class.java)
    private val branch5 = Mockito.mock(BranchInfo::class.java)

    private val mutation1 = Mockito.mock(MutationInfo::class.java)
    private val mutation2 = Mockito.mock(MutationInfo::class.java)
    private val mutation3 = Mockito.mock(MutationInfo::class.java)
    private val mutation4 = Mockito.mock(MutationInfo::class.java)
    private val mutation5 = Mockito.mock(MutationInfo::class.java)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        coverageVisualisationService = project.service()
        coverageToolWindowDisplayService = project.service()

        // Initialise the toolWindow TestGenie
        ToolWindowManager.getInstance(project)
            .registerToolWindow(RegisterToolWindowTask("TestGenie", ToolWindowAnchor.RIGHT))

        // Disable coverage visualisation
        val settingsState = ApplicationManager.getApplication().getService(TestGenieSettingsService::class.java).state
//        settingsState.showCoverage = false
    }

    @AfterEach
    override fun tearDown() {
        super.tearDown()
    }

    @ParameterizedTest
    @MethodSource("valueGenerator")
    fun fillToolWindowContents(
        className: String,
        coveredLinesSet: Set<Int>,
        uncoveredLinesSet: Set<Int>,
        coveredBranchesSet: Set<BranchInfo>,
        uncoveredBranchesSet: Set<BranchInfo>,
        coveredMutationSet: Set<MutationInfo>,
        uncoveredMutationSet: Set<MutationInfo>,
        lineCoverage: String,
        branchCoverage: String,
        mutationCoverage: String
    ) {
        val compactReport = CompactReport(TestGenerationResultImpl())
        compactReport.UUT = className
        compactReport.allCoveredLines = coveredLinesSet
        compactReport.allUncoveredLines = uncoveredLinesSet
        compactReport.allCoveredBranches = coveredBranchesSet
        compactReport.allUncoveredBranches = uncoveredBranchesSet
        compactReport.allCoveredMutation = coveredMutationSet
        compactReport.allUncoveredMutation = uncoveredMutationSet

        coverageVisualisationService.showCoverage(compactReport, myEditor)
        assertThat(className).isEqualTo(coverageToolWindowDisplayService.data[0])
        assertThat(lineCoverage).isEqualTo(coverageToolWindowDisplayService.data[1])
        assertThat(branchCoverage).isEqualTo(coverageToolWindowDisplayService.data[2])
        assertThat(mutationCoverage).isEqualTo(coverageToolWindowDisplayService.data[3])
    }

    @Test
    fun createToolWindowTabTestSingleContent() {
        coverageVisualisationService.showCoverage(CompactReport(TestGenerationResultImpl()), myEditor)
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("TestGenie")!!

        // Verify only 1 content is created
        assertThat(toolWindow.contentManager.contents.size).isEqualTo(1)
    }

    @Test
    fun createToolWindowTabTestContent() {
        coverageVisualisationService.showCoverage(CompactReport(TestGenerationResultImpl()), myEditor)
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("TestGenie")!!
        val content = toolWindow.contentManager.getContent(0)!!
        assertThat(content.displayName).isEqualTo("Coverage Visualisation")
    }

    private fun valueGenerator(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                "MyClass",
                setOf(1, 2),
                setOf(2, 3),
                setOf(branch1),
                setOf(branch2),
                setOf(mutation1, mutation2),
                setOf<MutationInfo>(),
                "50% (2/4)",
                "50% (1/2)",
                "100% (2/2)"
            ),
            Arguments.of(
                "MyClass",
                setOf(1, 2, 5),
                setOf(2, 3),
                setOf<BranchInfo>(),
                setOf(branch1, branch2),
                setOf(mutation1, mutation2),
                setOf(mutation3),
                "60% (3/5)",
                "0% (0/2)",
                "67% (2/3)"
            ),
            Arguments.of(
                "MyClass",
                setOf(1),
                setOf(2, 3, 4, 5, 6),
                setOf(branch5),
                setOf(branch1, branch2, branch3, branch4),
                setOf(mutation1),
                setOf(mutation3),
                "17% (1/6)",
                "20% (1/5)",
                "50% (1/2)"
            ),
            Arguments.of(
                "MyClass",
                setOf(1, 2, 3, 4, 5, 6, 7),
                setOf<Int>(),
                setOf(branch1, branch2, branch3, branch4, branch5),
                setOf<BranchInfo>(),
                setOf<MutationInfo>(),
                setOf(mutation1, mutation3),
                "100% (7/7)",
                "100% (5/5)",
                "0% (0/2)"
            ),
            Arguments.of(
                "MyClass",
                setOf<Int>(),
                setOf(1, 2, 3),
                setOf(branch1, branch2, branch3, branch4, branch5),
                setOf(branch1, branch2, branch3),
                setOf(mutation1, mutation2),
                setOf(mutation1, mutation2, mutation3, mutation4, mutation5),
                "0% (0/3)",
                "63% (5/8)",
                "29% (2/7)"
            )
        )
    }
}
