package org.jetbrains.research.testspark.display.generatedTestsTab

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.content.ContentFactory
import com.intellij.util.containers.stream
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.display.coverage.CoverageVisualisationTabBuilder
import org.jetbrains.research.testspark.display.ReportUpdater
import org.jetbrains.research.testspark.display.custom.IJProgressIndicator
import org.jetbrains.research.testspark.helpers.JavaClassBuilderHelper
import org.jetbrains.research.testspark.uiUtils.GenerateTestsTabHelper
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import java.util.LinkedList
import java.util.Locale
import java.util.Queue
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.SwingConstants

class GeneratedTestsTabBuilder(
    private val project: Project,
    private val report: Report,
    private val editor: Editor,
    private val uiContext: UIContext,
    private val coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder,
) {
    private var mainPanel: JPanel = JPanel()

    private var applyButton: JButton = JButton(PluginLabelsBundle.get("applyButton"))

    private val generatedTestsTabData: GeneratedTestsTabData = GeneratedTestsTabData()

    fun show() {
        generatedTestsTabData.topButtonsPanelBuilder = TopButtonsPanelBuilder()

        generatedTestsTabData.allTestCasePanel.removeAll()
        generatedTestsTabData.testCasePanels.clear()

        fillAllTestCasePanel()

        generatedTestsTabData.testsSelected = generatedTestsTabData.testCasePanels.size

        fillPanels()

        addActionListeners()

        createToolWindowTab()
    }

    fun getGeneratedTestsTabData() = generatedTestsTabData

    fun getRemoveAllButton() = generatedTestsTabData.topButtonsPanelBuilder.getRemoveAllButton()

    private fun addActionListeners() {
        applyButton.addActionListener { applyTests() }
        generatedTestsTabData.topButtonsPanelBuilder.getSelectAllButton()
            .addActionListener { toggleAllCheckboxes(true) }
        generatedTestsTabData.topButtonsPanelBuilder.getUnselectAllButton()
            .addActionListener { toggleAllCheckboxes(false) }
        generatedTestsTabData.topButtonsPanelBuilder.getRunAllButton().addActionListener { runAllTestCases() }
    }

    private fun fillPanels() {
        generatedTestsTabData.allTestCasePanel.layout =
            BoxLayout(generatedTestsTabData.allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()

        mainPanel.add(generatedTestsTabData.topButtonsPanelBuilder.getPanel(), BorderLayout.NORTH)
        mainPanel.add(generatedTestsTabData.scrollPane, BorderLayout.CENTER)

        applyButton.isOpaque = false
        applyButton.isContentAreaFilled = false
        mainPanel.add(applyButton, BorderLayout.SOUTH)
    }

    /**
     * Adds a separator to the allTestCasePanel.
     */
    private fun addSeparator() {
        generatedTestsTabData.allTestCasePanel.add(Box.createRigidArea(Dimension(0, 10)))
        generatedTestsTabData.allTestCasePanel.add(JSeparator(SwingConstants.HORIZONTAL))
        generatedTestsTabData.allTestCasePanel.add(Box.createRigidArea(Dimension(0, 10)))
    }

    private fun fillAllTestCasePanel() {
        addSeparator()

        report.testCaseList.values.forEach {
            val testCase = it
            val testCasePanel = JPanel()
            testCasePanel.layout = BorderLayout()

            // Add a checkbox to select the test
            val checkbox = JCheckBox()
            checkbox.isSelected = true
            checkbox.addItemListener {
                generatedTestsTabData.testsSelected -= (1 - 2 * checkbox.isSelected.compareTo(false))

                if (checkbox.isSelected) {
                    ReportUpdater.selectTestCase(
                        report,
                        testCase.id,
                        coverageVisualisationTabBuilder,
                        generatedTestsTabData,
                    )
                } else {
                    ReportUpdater.unselectTestCase(
                        report,
                        testCase.id,
                        coverageVisualisationTabBuilder,
                        generatedTestsTabData,
                    )
                }

                update()
            }

            testCasePanel.add(checkbox, BorderLayout.WEST)

            val testCasePanelBuilder = TestCasePanelBuilder(
                project,
                testCase,
                editor,
                checkbox,
                uiContext,
                report,
                coverageVisualisationTabBuilder,
                generatedTestsTabData,
            )

            testCasePanel.add(testCasePanelBuilder.getUpperPanel(), BorderLayout.NORTH)
            testCasePanel.add(testCasePanelBuilder.getMiddlePanel(), BorderLayout.CENTER)
            testCasePanel.add(testCasePanelBuilder.getBottomPanel(), BorderLayout.SOUTH)

            generatedTestsTabData.testCasePanelFactories.add(testCasePanelBuilder)

            testCasePanel.add(Box.createRigidArea(Dimension(12, 0)), BorderLayout.EAST)

            // Add panel to parent panel
            testCasePanel.maximumSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())

            generatedTestsTabData.allTestCasePanel.add(testCasePanel)

            addSeparator()

            generatedTestsTabData.testCasePanels[testCase.testName] = testCasePanel
        }

        generatedTestsTabData.testsSelected = generatedTestsTabData.testCasePanels.size

        generatedTestsTabData.topButtonsPanelBuilder.update(
            generatedTestsTabData.testCasePanels,
            generatedTestsTabData.testsSelected,
            generatedTestsTabData.testCasePanelFactories,
        )
    }

    /**
     * Updates the user interface of the tool window.
     *
     * This method updates the UI of the tool window tab by calling the updateUI
     * method of the allTestCasePanel object and the updateTopLabels method
     * of the topButtonsPanel object. It also checks if there are no more tests remaining
     * and closes the tool window if that is the case.
     */
    private fun update() {
        generatedTestsTabData.allTestCasePanel.updateUI()
        generatedTestsTabData.topButtonsPanelBuilder.update(
            generatedTestsTabData.testCasePanels,
            generatedTestsTabData.testsSelected,
            generatedTestsTabData.testCasePanelFactories,
        )
    }

    /**
     * Show a dialog where the user can select what test class the tests should be applied to,
     * and apply the selected tests to the test class.
     */
    private fun applyTests() {
        // Filter the selected test cases
        val selectedTestCasePanels =
            generatedTestsTabData.testCasePanels.filter { (it.value.getComponent(0) as JCheckBox).isSelected }
        val selectedTestCases = selectedTestCasePanels.map { it.key }

        // Get the test case components (source code of the tests)
        val testCaseComponents = selectedTestCases
            .map { GenerateTestsTabHelper.getEditorTextField(it, generatedTestsTabData)!! }
            .map { it.document.text }

        // Descriptor for choosing folders and java files
        val descriptor = FileChooserDescriptor(true, true, false, false, false, false)

        // Apply filter with folders and java files with main class
        WriteCommandAction.runWriteCommandAction(project) {
            descriptor.withFileFilter { file ->
                file.isDirectory || (
                    file.extension?.lowercase(Locale.getDefault()) == "java" && (
                        PsiManager.getInstance(project).findFile(file!!) as PsiJavaFile
                        ).classes.stream().map { it.name }
                        .toArray()
                        .contains(
                            (
                                PsiManager.getInstance(project)
                                    .findFile(file) as PsiJavaFile
                                ).name.removeSuffix(".java"),
                        )
                    )
            }
        }

        val fileChooser = FileChooser.chooseFiles(
            descriptor,
            project,
            LocalFileSystem.getInstance().findFileByPath(project.basePath!!),
        )

        /**
         * Cancel button pressed
         */
        if (fileChooser.isEmpty()) return

        /**
         * Chosen files by user
         */
        val chosenFile = fileChooser[0]

        /**
         * Virtual file of a final java file
         */
        var virtualFile: VirtualFile? = null

        /**
         * PsiClass of a final java file
         */
        var psiClass: PsiClass? = null

        /**
         * PsiJavaFile of a final java file
         */
        var psiJavaFile: PsiJavaFile? = null

        if (chosenFile.isDirectory) {
            // Input new file data
            var className: String
            var fileName: String
            var filePath: String
            // Waiting for correct file name input
            while (true) {
                val jOptionPane =
                    JOptionPane.showInputDialog(
                        null,
                        PluginLabelsBundle.get("optionPaneMessage"),
                        PluginLabelsBundle.get("optionPaneTitle"),
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        null,
                    )

                // Cancel button pressed
                jOptionPane ?: return

                // Get class name from user
                className = jOptionPane as String

                // Set file name and file path
                fileName = "${className.split('.')[0]}.java"
                filePath = "${chosenFile.path}/$fileName"

                // Check the correctness of a class name
                if (!Regex("[A-Z][a-zA-Z0-9]*(.java)?").matches(className)) {
                    showErrorWindow(PluginLabelsBundle.get("incorrectFileNameMessage"))
                    continue
                }

                // Check the existence of a file with this name
                if (File(filePath).exists()) {
                    showErrorWindow(PluginLabelsBundle.get("fileAlreadyExistsMessage"))
                    continue
                }
                break
            }

            // Create new file and set services of this file
            WriteCommandAction.runWriteCommandAction(project) {
                chosenFile.createChildData(null, fileName)
                virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$filePath")!!
                psiJavaFile = (PsiManager.getInstance(project).findFile(virtualFile!!) as PsiJavaFile)
                psiClass = PsiElementFactory.getInstance(project).createClass(className.split(".")[0])

                if (uiContext.testGenerationOutput.runWith.isNotEmpty()) {
                    psiClass!!.modifierList!!.addAnnotation("RunWith(${uiContext.testGenerationOutput.runWith})")
                }

                psiJavaFile!!.add(psiClass!!)
            }
        } else {
            // Set services of the chosen file
            virtualFile = chosenFile
            psiJavaFile = (PsiManager.getInstance(project).findFile(virtualFile!!) as PsiJavaFile)
            psiClass = psiJavaFile!!.classes[
                psiJavaFile!!.classes.stream().map { it.name }.toArray()
                    .indexOf(psiJavaFile!!.name.removeSuffix(".java")),
            ]
        }

        // Add tests to the file
        WriteCommandAction.runWriteCommandAction(project) {
            appendTestsToClass(testCaseComponents, psiClass!!, psiJavaFile!!)
        }

        // Remove the selected test cases from the cache and the tool window UI
        selectedTestCasePanels.forEach { GenerateTestsTabHelper.removeTestCase(it.key, generatedTestsTabData) }

        // Open the file after adding
        FileEditorManager.getInstance(project).openTextEditor(
            OpenFileDescriptor(project, virtualFile!!),
            true,
        )
    }

    /**
     * Removes all test cases from the cache and tool window UI.
     */
    private fun removeAllTestCases() {
        // Ask the user for the confirmation
        val choice = JOptionPane.showConfirmDialog(
            null,
            PluginMessagesBundle.get("removeAllMessage"),
            PluginMessagesBundle.get("confirmationTitle"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
        )

        // Cancel the operation if the user did not press "Yes"
        if (choice == JOptionPane.NO_OPTION) return

        clear()
    }

    /**
     * Executes all test cases.
     *
     * This method presents a caution message to the user and asks for confirmation before executing the test cases.
     * If the user confirms, it iterates through each test case panel factory and runs the corresponding test.
     */
    private fun runAllTestCases() {
        val choice = JOptionPane.showConfirmDialog(
            null,
            PluginMessagesBundle.get("runCautionMessage"),
            PluginMessagesBundle.get("confirmationTitle"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE,
        )

        if (choice == JOptionPane.CANCEL_OPTION) return

        generatedTestsTabData.topButtonsPanelBuilder.getRunAllButton().isEnabled = false

        // add each test generation task to queue
        val tasks: Queue<(CustomProgressIndicator) -> Unit> = LinkedList()

        for (testCasePanelFactory in generatedTestsTabData.testCasePanelFactories) {
            testCasePanelFactory.addTask(tasks)
        }
        // run tasks one after each other
        executeTasks(tasks)
    }

    private fun executeTasks(tasks: Queue<(CustomProgressIndicator) -> Unit>) {
        val nextTask = tasks.poll()

        nextTask?.let { task ->
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Test execution") {
                override fun run(indicator: ProgressIndicator) {
                    task(IJProgressIndicator(indicator))
                }

                override fun onFinished() {
                    super.onFinished()
                    executeTasks(tasks)
                }
            })
        }
    }

    /**
     * Append the provided test cases to the provided class.
     *
     * @param testCaseComponents the test cases to be appended
     * @param selectedClass the class which the test cases should be appended to
     * @param outputFile the output file for tests
     */
    private fun appendTestsToClass(testCaseComponents: List<String>, selectedClass: PsiClass, outputFile: PsiJavaFile) {
        // block document
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(
            PsiDocumentManager.getInstance(project).getDocument(outputFile)!!,
        )

        // insert tests to a code
        testCaseComponents.reversed().forEach {
            val testMethodCode =
                JavaClassBuilderHelper.getTestMethodCodeFromClassWithTestCase(
                    JavaClassBuilderHelper.formatJavaCode(
                        project,
                        it.replace("\r\n", "\n")
                            .replace("verifyException(", "// verifyException("),
                        uiContext.testGenerationOutput,
                    ),
                )
                    // Fix Windows line separators
                    .replace("\r\n", "\n")

            PsiDocumentManager.getInstance(project).getDocument(outputFile)!!.insertString(
                selectedClass.rBrace!!.textRange.startOffset,
                testMethodCode,
            )
        }

        // insert other info to a code
        PsiDocumentManager.getInstance(project).getDocument(outputFile)!!.insertString(
            selectedClass.rBrace!!.textRange.startOffset,
            uiContext.testGenerationOutput.otherInfo + "\n",
        )

        // insert imports to a code
        PsiDocumentManager.getInstance(project).getDocument(outputFile)!!.insertString(
            outputFile.importList?.startOffset ?: outputFile.packageStatement?.startOffset ?: 0,
            uiContext.testGenerationOutput.importsCode.joinToString("\n") + "\n\n",
        )

        // insert package to a code
        outputFile.packageStatement ?: PsiDocumentManager.getInstance(project).getDocument(outputFile)!!
            .insertString(
                0,
                if (uiContext.testGenerationOutput.packageLine.isEmpty()) {
                    ""
                } else {
                    "package ${uiContext.testGenerationOutput.packageLine};\n\n"
                },
            )
    }

    /**
     * Toggles check boxes so that they are either all selected or all not selected,
     *  depending on the provided parameter.
     *
     *  @param selected whether the checkboxes have to be selected or not
     */
    private fun toggleAllCheckboxes(selected: Boolean) {
        generatedTestsTabData.testCasePanels.forEach { (_, jPanel) ->
            val checkBox = jPanel.getComponent(0) as JCheckBox
            checkBox.isSelected = selected
        }
        generatedTestsTabData.testsSelected = if (selected) generatedTestsTabData.testCasePanels.size else 0

        update()
    }

    private fun showErrorWindow(message: String) {
        JOptionPane.showMessageDialog(
            null,
            message,
            PluginLabelsBundle.get("errorWindowTitle"),
            JOptionPane.ERROR_MESSAGE,
        )
    }

    fun clear() {
        generatedTestsTabData.testCasePanels.toMap()
            .forEach { GenerateTestsTabHelper.removeTestCase(it.key, generatedTestsTabData) }
        generatedTestsTabData.testCasePanelFactories.clear()

        generatedTestsTabData.contentManager?.removeContent(generatedTestsTabData.content!!, true)
    }

    /**
     * Creates a new toolWindow tab for the coverage visualisation.
     */
    private fun createToolWindowTab() {
        // Remove generated tests tab from content manager if necessary
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("TestSpark")
        generatedTestsTabData.contentManager = toolWindowManager!!.contentManager
        if (generatedTestsTabData.content != null) {
            generatedTestsTabData.contentManager!!.removeContent(generatedTestsTabData.content!!, true)
        }

        // If there is no generated tests tab, make it
        val contentFactory: ContentFactory = ContentFactory.getInstance()
        generatedTestsTabData.content = contentFactory.createContent(
            mainPanel,
            PluginLabelsBundle.get("generatedTests"),
            true,
        )
        generatedTestsTabData.contentManager!!.addContent(generatedTestsTabData.content!!)

        // Focus on generated tests tab and open toolWindow if not opened already
        generatedTestsTabData.contentManager!!.setSelectedContent(generatedTestsTabData.content!!)
        toolWindowManager.show()
    }
}
