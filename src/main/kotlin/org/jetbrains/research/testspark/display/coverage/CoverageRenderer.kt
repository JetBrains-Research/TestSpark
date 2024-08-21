package org.jetbrains.research.testspark.display.coverage

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.hint.HintManagerImpl
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.editor.markup.ActiveGutterRenderer
import com.intellij.openapi.editor.markup.LineMarkerRendererEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.HintHint
import com.intellij.ui.JBColor
import com.intellij.ui.LightweightHint
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.bundles.plugin.PluginSettingsBundle
import org.jetbrains.research.testspark.display.generatedTests.GeneratedTestsTabData
import org.jetbrains.research.testspark.services.EvoSuiteSettingsService
import org.jetbrains.research.testspark.services.PluginSettingsService
import org.jetbrains.research.testspark.settings.evosuite.EvoSuiteSettingsState
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseEvent
import javax.swing.JPanel

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
class CoverageRenderer(
    private val color: Color,
    private val lineNumber: Int,
    private val tests: List<String>,
    private val coveredMutation: List<String>,
    private val notCoveredMutation: List<String>,
    private val mapMutantsToTests: HashMap<String, MutableList<String>>,
    private val project: Project,
    private val generatedTestsTabData: GeneratedTestsTabData,
) :
    ActiveGutterRenderer, LineMarkerRendererEx {
    private val evoSuiteSettingsState: EvoSuiteSettingsState
        get() = project.getService(EvoSuiteSettingsService::class.java).state

    private var defaultEditorColor: Color? = null

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
                    highlightTestCase(testName)
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
     * Use Display service's mutant highlighter function
     * @param mutantName name of the mutant whose coverage to visualise
     * @param map map of mutant operations -> List of names of tests which cover the mutants
     */
    private fun highlightMutantsInToolwindow(mutantName: String, map: HashMap<String, MutableList<String>>) {
        highlightCoveredMutants(map.getOrPut(mutantName) { ArrayList() })
    }

    /**
     * Highlight the mini-editor in the tool window whose name corresponds with the name of the test provided
     *
     * @param name name of the test whose editor should be highlighted
     */
    private fun highlightTestCase(name: String) {
        val myPanel = generatedTestsTabData.testCaseNameToPanel[name] ?: return
        openToolWindowTab()
        scrollToPanel(myPanel)

        val editorTextField = generatedTestsTabData.testCaseNameToEditorTextField[name] ?: return
        val settingsProjectState = project.service<PluginSettingsService>().state
        val highlightColor =
            JBColor(
                PluginSettingsBundle.get("colorName"),
                Color(
                    settingsProjectState.colorRed,
                    settingsProjectState.colorGreen,
                    settingsProjectState.colorBlue,
                    30,
                ),
            )
        if (editorTextField.background.equals(highlightColor)) return
        defaultEditorColor = editorTextField.background
        editorTextField.background = highlightColor
        returnOriginalEditorBackground(editorTextField)
    }

    /**
     * Method to open the toolwindow tab with generated tests if not already open.
     */
    private fun openToolWindowTab() {
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("TestSpark")
        generatedTestsTabData.contentManager = toolWindowManager!!.contentManager
        if (generatedTestsTabData.content != null) {
            toolWindowManager.show()
            toolWindowManager.contentManager.setSelectedContent(generatedTestsTabData.content!!)
        }
    }

    /**
     * Scrolls to the highlighted panel.
     *
     * @param myPanel the panel to scroll to
     */
    private fun scrollToPanel(myPanel: JPanel) {
        var sum = 0
        for (component in generatedTestsTabData.allTestCasePanel.components) {
            if (component == myPanel) {
                break
            }
            sum += component.height
        }
        val scroll = generatedTestsTabData.scrollPane.verticalScrollBar

        // Get the value of the "myPanel" height to enable scrolling to that position.
        // The current scroll percentage relative to the "myPanel" height is calculated as (sum / generatedTestsTabData.allTestCasePanel.height).
        // The total scroll height is the sum of the minimum and maximum scroll values (scroll.minimum + scroll.maximum).
        scroll.value = (scroll.minimum + scroll.maximum) * sum / generatedTestsTabData.allTestCasePanel.height
    }

    /**
     * Reset the provided editors color to the default (initial) one after 10 seconds
     * @param editor the editor whose color to change
     */
    private fun returnOriginalEditorBackground(editor: EditorTextField) {
        Thread {
            val timeWithHighlightedBackground: Long = 10000
            Thread.sleep(timeWithHighlightedBackground)
            editor.background = defaultEditorColor
        }.start()
    }

    /**
     * Highlight a range of editors
     * @param names list of test names to pass to highlight function
     */
    private fun highlightCoveredMutants(names: List<String>) {
        names.forEach {
            highlightTestCase(it)
        }
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
