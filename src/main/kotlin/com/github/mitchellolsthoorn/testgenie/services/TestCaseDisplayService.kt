package com.github.mitchellolsthoorn.testgenie.services;

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaCodeFragmentFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiExpressionCodeFragment
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class TestCaseDisplayService(private val project: Project) {

    val mainPanel: JPanel = JPanel()
    private val applyButton: JButton = JButton("Apply")
    private val allTestCasePanel: JPanel = JPanel()
    private val scrollPane: JBScrollPane = JBScrollPane(allTestCasePanel)

    init {
        allTestCasePanel.layout = BoxLayout(allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()
        mainPanel.add(applyButton, BorderLayout.SOUTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)
    }

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     *
     * @param testCases The test cases to display
     */
    fun displayTestCases(testCases: List<String>) {
        allTestCasePanel.removeAll()
        testCases.forEach {
            val testCasePanel = JPanel()
            testCasePanel.layout = BorderLayout()

            val checkbox = JCheckBox()
            checkbox.isSelected = true
            testCasePanel.add(checkbox, BorderLayout.WEST)

            val code = JavaCodeFragmentFactory.getInstance(project)
                .createExpressionCodeFragment(it, null, null, true)
            val document = PsiDocumentManager.getInstance(project).getDocument(code)
            val editor = EditorTextField(document, project, JavaFileType.INSTANCE);

            editor.setOneLineMode(false)
            editor.isViewer = true
            editor.preferredSize = Dimension(0, 200)
            // TODO: add scroll bar

            testCasePanel.add(editor, BorderLayout.CENTER)

            testCasePanel.maximumSize = Dimension(Short.MAX_VALUE.toInt(), testCasePanel.preferredSize.height)
            allTestCasePanel.add(testCasePanel)
            allTestCasePanel.add(Box.createRigidArea(Dimension(0, 5)))
        }
    }
}
