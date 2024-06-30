package org.jetbrains.research.testspark.helpers

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.hint.HintManagerImpl
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.editor.markup.ActiveGutterRenderer
import com.intellij.openapi.editor.markup.LineMarkerRendererEx
import com.intellij.openapi.project.Project
import com.intellij.ui.HintHint
import com.intellij.ui.LightweightHint
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.services.EvoSuiteSettingsService
import org.jetbrains.research.testspark.services.java.JavaTestCaseDisplayService
import org.jetbrains.research.testspark.settings.evosuite.EvoSuiteSettingsState
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseEvent

/**
 * This class extends the line marker and gutter editor to allow more functionality.
 *
 * @param color color of marker
 * @param lineNumber lineNumber to color
 * @param tests list of tests that cover this line
 * @param coveredMutation list of mutant operation which are covered by tests
 * @param notCoveredMutation list of mutant operation which are not covered by tests
 * @param mapMutantsToTests map of mutant operation -> List of names of tests which cover mutant
 * @param project the current project
 */
class CoverageHelper(
    private val color: Color,
    private val lineNumber: Int,
    private val tests: List<String>,
    private val coveredMutation: List<String>,
    private val notCoveredMutation: List<String>,
    private val mapMutantsToTests: HashMap<String, MutableList<String>>,
    private val project: Project,
) :
    ActiveGutterRenderer, LineMarkerRendererEx {
    private val evoSuiteSettingsState: EvoSuiteSettingsState
        get() = project.getService(EvoSuiteSettingsService::class.java).state

    /**
     * Perform the action - show toolTip on mouse click.
     *
     * @param editor the editor
     * @param e mouse event
     */
    override fun doAction(editor: Editor, e: MouseEvent) {
        e.consume()
        val prePanel = FormBuilder
            .createFormBuilder()
            .addComponent(JBLabel(" Covered by tests:"), 10)

        for (testName in tests) {
            prePanel.addComponent(
                ActionLink(testName) {
                    highlightInToolwindow(testName)
                },
            )
        }

        if (coveredMutation.isNotEmpty() && evoSuiteSettingsState.criterionWeakMutation) {
            prePanel.addComponent(JBLabel(" Killed mutants:"), 10)
            for (mutantName in coveredMutation) {
                prePanel.addComponent(
                    ActionLink(mutantName.substringBefore('(')) {
                        highlightMutantsInToolwindow(mutantName, mapMutantsToTests)
                    },
                )
            }
        }

        if (notCoveredMutation.isNotEmpty() && evoSuiteSettingsState.criterionWeakMutation) {
            prePanel.addComponent(JBLabel(" Survived mutants:"), 10)
            for (mutantName in notCoveredMutation) {
                prePanel.addComponent(
                    JBLabel(mutantName.substringBefore('(')),
                )
            }
        }

        val panel = JBScrollPane(
            prePanel.addVerticalGap(10).panel,
            JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
        )

        panel.preferredSize = Dimension(panel.preferredSize.width, 400.coerceAtMost(panel.preferredSize.height))

        val hint = LightweightHint(panel)
        val point = HintManagerImpl.getHintPosition(hint, editor, LogicalPosition(lineNumber, 0), HintManager.RIGHT)
        HintManagerImpl.getInstanceImpl().showEditorHint(
            hint,
            editor,
            point,
            HintManager.HIDE_BY_ANY_KEY or HintManager.HIDE_BY_TEXT_CHANGE or HintManager.HIDE_BY_OTHER_HINT or HintManager.HIDE_BY_SCROLLING,
            -1,
            false,
            HintHint(editor, point),
        )
    }

    /**
     * Test if action can be performed - mouse is within the gutter.
     *
     * @param editor the editor
     * @param e mouse event
     * @return true iff mouse has interacted with gutter
     */
    override fun canDoAction(editor: Editor, e: MouseEvent): Boolean {
        val component = e.component
        if (component is EditorGutterComponentEx) {
            return e.x > component.lineMarkerAreaOffset && e.x < component.iconAreaOffset
        }
        return false
    }

    /**
     * Use Display service's mini-editor highlighter function
     *
     * @param name name of the test to highlight
     */
    private fun highlightInToolwindow(name: String) {
        val testCaseDisplayService = project.service<JavaTestCaseDisplayService>()

        testCaseDisplayService.highlightTestCase(name)
    }

    /**
     * Use Display service's mutant highlighter function
     * @param mutantName name of the mutant whose coverage to visualise
     * @param map map of mutant operations -> List of names of tests which cover the mutants
     */
    private fun highlightMutantsInToolwindow(mutantName: String, map: HashMap<String, MutableList<String>>) {
        val testCaseDisplayService = project.service<JavaTestCaseDisplayService>()

        testCaseDisplayService.highlightCoveredMutants(map.getOrPut(mutantName) { ArrayList() })
    }

    /**
     * Sets area to paint in given color.
     *
     * @param editor the editor
     * @param g graphics used to draw
     * @param r rectangle object
     */
    override fun paint(editor: Editor, g: Graphics, r: Rectangle) {
        g.fillRect(r.x, r.y, r.width * 3 / 2, r.height * 5 / 6)
        g.color = color
    }

    /**
     * Getter for offset of gutter color.
     *
     * @return position
     */
    override fun getPosition(): LineMarkerRendererEx.Position {
        return LineMarkerRendererEx.Position.LEFT
    }
}
