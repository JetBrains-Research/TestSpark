package org.jetbrains.research.testspark.display

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentManager
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.collectors.data.DataToCollect
import org.jetbrains.research.testspark.collectors.data.UserExperienceCollectors
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.display.coverage.CoverageVisualisationTabBuilder
import org.jetbrains.research.testspark.display.generatedTestsTab.GeneratedTestsTabBuilder
import javax.swing.JOptionPane

class TestSparkDisplayBuilder {
    private var toolWindow: ToolWindow? = null
    private var contentManager: ContentManager? = null

    private var editor: Editor? = null

    private var coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder? = null
    private var generatedTestsTabBuilder: GeneratedTestsTabBuilder? = null

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     * Add Tests and their names to a List of pairs (used for highlighting)
     */
    fun display(report: Report, editor: Editor, uiContext: UIContext, project: Project, userExperienceCollectors: UserExperienceCollectors, dataToCollect: DataToCollect) {
        this.toolWindow = ToolWindowManager.getInstance(project).getToolWindow("TestSpark")
        this.contentManager = toolWindow!!.contentManager

        this.editor = editor

        coverageVisualisationTabBuilder = CoverageVisualisationTabBuilder(project, editor, userExperienceCollectors, dataToCollect)
        generatedTestsTabBuilder = GeneratedTestsTabBuilder(project, report, editor, uiContext, coverageVisualisationTabBuilder!!, userExperienceCollectors, dataToCollect)

        generatedTestsTabBuilder!!.show(contentManager!!)
        coverageVisualisationTabBuilder!!.show(report, generatedTestsTabBuilder!!.getGeneratedTestsTabData())

        toolWindow!!.show()

        generatedTestsTabBuilder!!.getRemoveAllButton().addActionListener {
            if (generatedTestsTabBuilder!!.getGeneratedTestsTabData().testCaseNameToPanel.isEmpty()) {
                clear()
                return@addActionListener
            }

            val choice = JOptionPane.showConfirmDialog(
                null,
                PluginMessagesBundle.get("removeAllCautionMessage"),
                PluginMessagesBundle.get("confirmationTitle"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
            )

            if (choice == JOptionPane.OK_OPTION) {
                clear()
            }
        }

        // Add collector logging
        val testGenerationElapsedTime = System.currentTimeMillis() - dataToCollect.testGenerationStartTime!!
        userExperienceCollectors.testGenerationFinishedCollector.logEvent(
            testGenerationElapsedTime,
            dataToCollect.technique!!,
            dataToCollect.codeType!!,
        )

        // Add collector logging
        userExperienceCollectors.generatedTestsCollector.logEvent(
            report.testCaseList.size,
            dataToCollect.technique!!,
            dataToCollect.codeType!!,
        )
    }

    fun clear() {
        editor?.markupModel?.removeAllHighlighters()

        coverageVisualisationTabBuilder?.clear()
        generatedTestsTabBuilder?.clear()

        if (contentManager != null) {
            for (content in contentManager!!.contents) {
                if (content.tabName != PluginLabelsBundle.get("descriptionWindow")) {
                    contentManager?.removeContent(content, true)
                }
            }
        }

        toolWindow?.hide()
    }
}
