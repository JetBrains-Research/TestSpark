package org.jetbrains.research.testspark.actions.llm

import com.intellij.lang.Language
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.containers.stream
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.template.ToolPanelFactory
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.display.TestCaseDocumentCreator
import org.jetbrains.research.testspark.services.TestSamplesService
import java.awt.Font
import javax.swing.ButtonGroup
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.ScrollPaneConstants

class LLMSampleSelectorFactory(private val project: Project) : ToolPanelFactory {
    private val selectionTypeButtons: MutableList<JRadioButton> = mutableListOf(
        JRadioButton(TestSparkLabelsBundle.defaultValue("provideTestSample")),
        JRadioButton(TestSparkLabelsBundle.defaultValue("selectTestSample")),
        JRadioButton(TestSparkLabelsBundle.defaultValue("noTestSample")),
    )
    private val selectionTypeButtonGroup = ButtonGroup()

    private val radioButtonsPanel = JPanel()

    private var testSamplesSelector = ComboBox(arrayOf(""))
    private val backLlmButton = JButton(TestSparkLabelsBundle.defaultValue("back"))
    private val nextButton = JButton(TestSparkLabelsBundle.defaultValue("ok"))

    private val languageTextField = LanguageTextField(
        Language.findLanguageByID("JAVA"),
        project,
        createTestSampleClass("// provide test method code here"),
        TestCaseDocumentCreator("TestSample"),
        false,
    )

    private val languageTextFieldScrollPane = JBScrollPane(
        languageTextField,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS,
    )

    init {
        collectTestSamples()

        // TODO provide correct array
        testSamplesSelector.model = DefaultComboBoxModel(arrayOf("None") + arrayOf("A", "B", "C"))
//        testSamplesSelector = ComboBox(arrayOf("Please select") + project.service<TestSamplesService>().testSamples.toTypedArray())
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

    /**
     * Retrieves the LLM panel.
     *
     * @return The JPanel object representing the LLM setup panel.
     */
    override fun getPanel(): JPanel {
        val textTitle = JLabel(TestSparkLabelsBundle.defaultValue("llmSampleSelectorFactory"))
        textTitle.font = Font("Monochrome", Font.BOLD, 20)

        val titlePanel = JPanel()
        titlePanel.add(textTitle)

        for (button in selectionTypeButtons) {
            selectionTypeButtonGroup.add(button)
            radioButtonsPanel.add(button)
        }

        val mainPanel = FormBuilder.createFormBuilder()
            .addComponentFillVertically(radioButtonsPanel, 10)
            .addComponentFillVertically(testSamplesSelector, 10)
            .addComponentFillVertically(languageTextFieldScrollPane, 10)
            .panel

        val bottomButtons = JPanel()
        backLlmButton.isOpaque = false
        backLlmButton.isContentAreaFilled = false
        bottomButtons.add(backLlmButton)
        nextButton.isOpaque = false
        nextButton.isContentAreaFilled = false
        bottomButtons.add(nextButton)

        return FormBuilder.createFormBuilder()
            .setFormLeftIndent(10)
            .addVerticalGap(5)
            .addComponent(titlePanel)
            .addComponent(mainPanel)
            .addComponentFillVertically(bottomButtons, 10)
            .panel
    }

    override fun applyUpdates() {
        // TODO implement
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
                psiClass.allMethods.forEach { method ->
                    val annotations = method.modifierList.annotations
                    annotations.forEach { annotation ->
                        if (annotation.qualifiedName == "org.junit.jupiter.api.Test" || annotation.qualifiedName == "org.junit.Test") {
                            // TODO next line is incorrect for some reason
                            project.service<TestSamplesService>().testSamples.plus(method.name)
                            println(method.name)
                            println(method.text)
                        }
                    }
                }
            }
            true
        }
    }

    private fun createTestSampleClass(methodCode: String): String {
        return "public class TestSample {\n" +
            "   $methodCode\n" +
            "}"
    }
}
