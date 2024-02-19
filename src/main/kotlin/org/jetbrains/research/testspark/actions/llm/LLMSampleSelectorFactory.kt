package org.jetbrains.research.testspark.actions.llm

import com.intellij.lang.Language
import com.intellij.openapi.diff.DiffColors
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.containers.stream
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.template.PanelFactory
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.display.TestCaseDocumentCreator
import org.jetbrains.research.testspark.display.TestSparkIcons
import org.jetbrains.research.testspark.display.createButton
import org.jetbrains.research.testspark.display.getModifiedLines
import java.awt.Font
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.ScrollPaneConstants

class LLMSampleSelectorFactory(private val project: Project) : PanelFactory {
    private val selectionTypeButtons: MutableList<JRadioButton> = mutableListOf(
        JRadioButton(TestSparkLabelsBundle.defaultValue("provideTestSample")),
        JRadioButton(TestSparkLabelsBundle.defaultValue("noTestSample")),
    )
    private val selectionTypeButtonGroup = ButtonGroup()

    private val radioButtonsPanel = JPanel()

    private var testSamplesSelector = ComboBox(arrayOf(""))
    private val backLlmButton = JButton(TestSparkLabelsBundle.defaultValue("back"))
    private val nextButton = JButton(TestSparkLabelsBundle.defaultValue("ok"))

    private val testNames = mutableListOf("<html><b>provide manually</b></html>")
    private val initialTestCodes = mutableListOf(createTestSampleClass("", "// provide test method code here"))
    private val currentTestCodes = mutableListOf(createTestSampleClass("", "// provide test method code here"))

    private val languageTextField = LanguageTextField(
        Language.findLanguageByID("JAVA"),
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

    private val resetButton = createButton(TestSparkIcons.reset, TestSparkLabelsBundle.defaultValue("resetTip"))

    private val selectionLabel = JBLabel(TestSparkLabelsBundle.defaultValue("selectSamples"))

    init {
        addCollectors()

        collectTestSamples()

        testSamplesSelector.model = DefaultComboBoxModel(testNames.toTypedArray())
    }

    /**
     * Adds action listeners to the selectionTypeButtons array to enable the nextButton if any button is selected.
     */
    private fun addCollectors() {
        languageTextField.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                languageTextField.editor?.markupModel?.removeAllHighlighters()

                for (index in testNames.indices) {
                    if (testNames[index] == testSamplesSelector.selectedItem) {
                        currentTestCodes[index] = languageTextField.text

                        val modifiedLineIndexes = getModifiedLines(
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
            }
        })

        selectionTypeButtons[0].addActionListener {
            nextButton.isEnabled = true
            enabledComponents(true)
        }

        selectionTypeButtons[1].addActionListener {
            nextButton.isEnabled = true
            enabledComponents(false)
        }

        testSamplesSelector.addActionListener {
            for (index in testNames.indices) {
                if (testNames[index] == testSamplesSelector.selectedItem) {
                    languageTextField.text = currentTestCodes[index]
                }
            }
        }

        resetButton.addActionListener {
            for (index in testNames.indices) {
                if (testNames[index] == testSamplesSelector.selectedItem) {
                    currentTestCodes[index] = initialTestCodes[index]
                    languageTextField.text = currentTestCodes[index]
                }
            }
        }
    }

    /**
     * Returns a JPanel object representing the title panel.
     * The panel contains a JLabel with the text "llmSampleSelectorFactory",
     * rendered in a bold 20pt Monochrome font.
     *
     * @return a JPanel object representing the title panel
     */
    override fun getTitlePanel(): JPanel {
        val textTitle = JLabel(TestSparkLabelsBundle.defaultValue("llmSampleSelectorFactory"))
        textTitle.font = Font("Monochrome", Font.BOLD, 20)

        val titlePanel = JPanel()
        titlePanel.add(textTitle)

        return titlePanel
    }

    /**
     * Returns the middle panel containing radio buttons, a test samples selector, and a language text field scroll pane.
     *
     * @return the middle panel as a JPanel
     */
    override fun getMiddlePanel(): JPanel {
        for (button in selectionTypeButtons) {
            selectionTypeButtonGroup.add(button)
            radioButtonsPanel.add(button)
        }

        val testSamplesAndResetButtonPanel = JPanel()
        testSamplesAndResetButtonPanel.layout = BoxLayout(testSamplesAndResetButtonPanel, BoxLayout.X_AXIS)
        testSamplesAndResetButtonPanel.add(testSamplesSelector)
        testSamplesAndResetButtonPanel.add(resetButton)

        enabledComponents(false)

        return FormBuilder.createFormBuilder()
            .setFormLeftIndent(10)
            .addComponent(JPanel(), 0)
            .addComponent(radioButtonsPanel, 10)
            .addLabeledComponent(
                selectionLabel,
                testSamplesAndResetButtonPanel,
                10,
                false,
            )
            .addComponentFillVertically(languageTextFieldScrollPane, 10)
            .panel
    }

    /**
     * Retrieves the bottom panel containing the back and next buttons.
     *
     * @return The JPanel containing the back and next buttons.
     */
    override fun getBottomPanel(): JPanel {
        val bottomPanel = JPanel()
        backLlmButton.isOpaque = false
        backLlmButton.isContentAreaFilled = false
        bottomPanel.add(backLlmButton)
        nextButton.isOpaque = false
        nextButton.isContentAreaFilled = false
        nextButton.isEnabled = false
        bottomPanel.add(nextButton)

        return bottomPanel
    }

    /**
     * Retrieves the back button.
     *
     * @return The back button.
     */
    override fun getBackButton() = backLlmButton

    /**
     * Retrieves the reference to the "OK" button.
     *
     * @return The reference to the "OK" button.
     */
    override fun getFinishedButton() = nextButton

    override fun applyUpdates() {
        // TODO implement adding sample (languageTextField.text) to the prompt
    }

    /**
     * Retrieves a list of test samples from the given project.
     *
     * @return A list of strings, representing the names of the test samples.
     */
    private fun collectTestSamples() {
        val projectFileIndex: ProjectFileIndex = ProjectRootManager.getInstance(project).fileIndex
        val javaFileType: FileType = FileTypeManager.getInstance().getFileTypeByExtension("java")

        projectFileIndex.iterateContent { file ->
            if (file.fileType === javaFileType) {
                val psiJavaFile = (PsiManager.getInstance(project).findFile(file) as PsiJavaFile)
                val psiClass = psiJavaFile.classes[
                    psiJavaFile.classes.stream().map { it.name }.toArray()
                        .indexOf(psiJavaFile.name.removeSuffix(".java")),
                ]
                val imports = psiJavaFile.importList?.allImportStatements?.map { it.text }?.toList()
                    ?.joinToString("\n") ?: ""
                psiClass.allMethods.forEach { method ->
                    val annotations = method.modifierList.annotations
                    annotations.forEach { annotation ->
                        if (annotation.qualifiedName == "org.junit.jupiter.api.Test" || annotation.qualifiedName == "org.junit.Test") {
                            val code: String = createTestSampleClass(imports, method.text)
                            testNames.add(createMethodName(method.name))
                            initialTestCodes.add(code)
                            currentTestCodes.add(code)
                        }
                    }
                }
            }
            true
        }
    }

    private fun createTestSampleClass(imports: String, methodCode: String): String {
        var normalizedImports = imports
        if (normalizedImports.isNotBlank()) normalizedImports += "\n\n"
        return normalizedImports +
            "public class TestSample {\n" +
            "   $methodCode\n" +
            "}"
    }

    private fun createMethodName(methodName: String): String =
        "<html><b><font color='orange'>method</font> $methodName</b></html>"

    private fun enabledComponents(isEnabled: Boolean) {
        resetButton.isEnabled = false
        selectionLabel.isEnabled = isEnabled
        testSamplesSelector.isEnabled = isEnabled
        languageTextField.isEnabled = isEnabled
        languageTextFieldScrollPane.isEnabled = isEnabled
    }
}