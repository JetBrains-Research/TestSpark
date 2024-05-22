package org.jetbrains.research.testspark.display

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.data.CollectorsData
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.display.coverage.CoverageVisualisationTabBuilder
import org.jetbrains.research.testspark.display.generatedTestsTab.GeneratedTestsTabBuilder
import javax.swing.JOptionPane

class TestSparkDisplayBuilder {
    private var editor: Editor? = null

    private var coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder? = null
    private var generatedTestsTabBuilder: GeneratedTestsTabBuilder? = null

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     * Add Tests and their names to a List of pairs (used for highlighting)
     */
    fun display(report: Report, editor: Editor, uiContext: UIContext, project: Project, collectorsData: CollectorsData) {
        this.editor = editor

        coverageVisualisationTabBuilder = CoverageVisualisationTabBuilder(project, editor, collectorsData)
        generatedTestsTabBuilder = GeneratedTestsTabBuilder(project, report, editor, uiContext, coverageVisualisationTabBuilder!!, collectorsData)

        coverageVisualisationTabBuilder!!.show(report, generatedTestsTabBuilder!!.getGeneratedTestsTabData())
        generatedTestsTabBuilder!!.show()

        generatedTestsTabBuilder!!.getRemoveAllButton().addActionListener {
            if (generatedTestsTabBuilder!!.getGeneratedTestsTabData().testCaseNameToPanels.isEmpty()) {
                clear(project)
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
                clear(project)
            }
        }

        // Add collector logging
        collectorsData.testGenerationFinishedCollector.logEvent(
            System.currentTimeMillis() - collectorsData.testGenerationStartTime!!,
            collectorsData.technique!!,
            collectorsData.codeType!!,
        )

        // Add collector logging
        collectorsData.generatedTestsCollector.logEvent(
            report.testCaseList.size,
            collectorsData.technique!!,
            collectorsData.codeType!!,
        )
    }

    fun clear(project: Project) {
        editor?.markupModel?.removeAllHighlighters()

        coverageVisualisationTabBuilder?.clear()
        generatedTestsTabBuilder?.clear()

        ToolWindowManager.getInstance(project).getToolWindow("TestSpark")?.hide()
    }
}
