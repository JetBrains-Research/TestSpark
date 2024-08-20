package org.jetbrains.research.testspark.actions.llm

import com.intellij.lang.Language
import com.intellij.openapi.diff.DiffColors
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBScrollPane
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.display.TestCaseDocumentCreator
import org.jetbrains.research.testspark.display.TestSparkIcons
import org.jetbrains.research.testspark.display.utils.IconButtonCreator
import org.jetbrains.research.testspark.display.utils.ModifiedLinesGetter
import javax.swing.BoxLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

class TestSamplePanelBuilder(
    project: Project,
    private val middlePanel: JPanel,
    private val testNames: MutableList<String>,
    private val initialTestCodes: MutableList<String>,
    private val language: SupportedLanguage,
) {
    // init components
    private val currentTestCodes = initialTestCodes.toMutableList()
    private val languageTextField = LanguageTextField(
        Language.findLanguageByID(language.languageId),
        project,
        initialTestCodes[0],
        TestCaseDocumentCreator("TestSample"),
        false,
    )
    private var languageTextFieldScrollPane = JBScrollPane(
        languageTextField,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS,
    )
    private var testSamplesSelector = ComboBox(arrayOf(""))
    private val resetButton = IconButtonCreator.getButton(TestSparkIcons.reset, PluginLabelsBundle.get("resetTip"))
    private val removeButton = IconButtonCreator.getButton(TestSparkIcons.remove, PluginLabelsBundle.get("removeTip"))

    init {
        addListeners()

        testSamplesSelector.model = DefaultComboBoxModel(testNames.toTypedArray())
    }

    /**
     * Add listeners.
     */
    private fun addListeners() {
        languageTextField.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                languageTextField.editor?.markupModel?.removeAllHighlighters()

                for (index in testNames.indices) {
                    if (testNames[index] == testSamplesSelector.selectedItem) {
                        currentTestCodes[index] = languageTextField.text

                        val modifiedLineIndexes = ModifiedLinesGetter.getLines(
                            initialTestCodes[index].split("\n"),
                            currentTestCodes[index].split("\n"),
                        )

                        for (line in modifiedLineIndexes) {
                            languageTextField.editor!!.markupModel.addLineHighlighter(
                                DiffColors.DIFF_MODIFIED,
                                line,
                                HighlighterLayer.FIRST,
                            )
                        }

                        resetButton.isEnabled = initialTestCodes[index] != currentTestCodes[index]
                    }
                }
                middlePanel.revalidate()
            }
        })

        testSamplesSelector.addActionListener {
            for (index in testNames.indices) {
                if (testNames[index] == testSamplesSelector.selectedItem) {
                    languageTextField.text = currentTestCodes[index]
                }
            }
            middlePanel.revalidate()
        }

        resetButton.addActionListener {
            for (index in testNames.indices) {
                if (testNames[index] == testSamplesSelector.selectedItem) {
                    currentTestCodes[index] = initialTestCodes[index]
                    languageTextField.text = currentTestCodes[index]
                }
            }
            middlePanel.revalidate()
        }
    }

    /**
     * @return removeButton
     */
    fun getRemoveButton(): JButton = removeButton

    /**
     *
     */
    fun getTestSamplePanel(): JPanel {
        val testSamplePanel = JPanel()

        testSamplePanel.layout = BoxLayout(testSamplePanel, BoxLayout.X_AXIS)
        testSamplePanel.add(testSamplesSelector)
        testSamplePanel.add(resetButton)
        testSamplePanel.add(removeButton)

        return testSamplePanel
    }

    /**
     * @return languageTextFieldScrollPane
     */
    fun getCodeScrollPanel(): JBScrollPane {
        return languageTextFieldScrollPane
    }

    /**
     * Enables and disables the components in the panel in case of type button selection.
     */
    fun enabledComponents(isEnabled: Boolean) {
        resetButton.isEnabled = false
        for (index in testNames.indices) {
            if (testNames[index] == testSamplesSelector.selectedItem && isEnabled) {
                resetButton.isEnabled = initialTestCodes[index] != currentTestCodes[index]
            }
        }
        removeButton.isEnabled = isEnabled
        testSamplesSelector.isEnabled = isEnabled
        languageTextField.isEnabled = isEnabled
        languageTextFieldScrollPane.isEnabled = isEnabled
    }

    /**
     * @return code of the sample
     */
    fun getCode(): String = languageTextField.text
}
