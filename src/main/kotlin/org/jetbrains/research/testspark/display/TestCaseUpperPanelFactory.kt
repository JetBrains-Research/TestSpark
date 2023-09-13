package org.jetbrains.research.testspark.display

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.services.TestCaseDisplayService
import org.jetbrains.research.testspark.services.TestsExecutionResultService
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel

class TestCaseUpperPanelFactory(
    private val project: Project,
    private val testCaseName: String,
    private val checkbox: JCheckBox,
) {
    private val panel = JPanel()
    private val errorLabel = JLabel(TestSparkIcons.showError)
    private val copyButton = createButton(TestSparkIcons.copy, TestSparkLabelsBundle.defaultValue("copyTip"))
    private val likeButton = createButton(TestSparkIcons.like, TestSparkLabelsBundle.defaultValue("likeTip"))
    private val dislikeButton = createButton(TestSparkIcons.dislike, TestSparkLabelsBundle.defaultValue("dislikeTip"))

    fun getPanel(): JPanel {
        updateErrorLabel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(Box.createRigidArea(Dimension(checkbox.preferredSize.width, checkbox.preferredSize.height)))
        panel.add(errorLabel)
        panel.add(Box.createHorizontalGlue())
        panel.add(copyButton)
        panel.add(Box.createRigidArea(Dimension(5, 0)))
        panel.add(likeButton)
        panel.add(Box.createRigidArea(Dimension(5, 0)))
        panel.add(dislikeButton)
        panel.add(Box.createRigidArea(Dimension(10, 0)))

        likeButton.addActionListener {
            if (likeButton.icon == TestSparkIcons.likeSelected) {
                likeButton.icon = TestSparkIcons.like
            } else if (likeButton.icon == TestSparkIcons.like) {
                likeButton.icon = TestSparkIcons.likeSelected
            }
            dislikeButton.icon = TestSparkIcons.dislike
//            TODO add implementation
        }

        dislikeButton.addActionListener {
            if (dislikeButton.icon == TestSparkIcons.dislikeSelected) {
                dislikeButton.icon = TestSparkIcons.dislike
            } else if (dislikeButton.icon == TestSparkIcons.dislike) {
                dislikeButton.icon = TestSparkIcons.dislikeSelected
            }
            likeButton.icon = TestSparkIcons.like
//            TODO add implementation
        }

        copyButton.addActionListener {
            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(
                StringSelection(
                    project.service<TestCaseDisplayService>().getEditor(testCaseName)!!.document.text,
                ),
                null,
            )
        }

        return panel
    }

    /**
     * Updates the error label with a new message.
     */
    fun updateErrorLabel() {
        val error = project.service<TestsExecutionResultService>().getCurrentError(testCaseName)
        if (error.isBlank()) {
            errorLabel.isVisible = false
        } else {
            errorLabel.isVisible = true
            errorLabel.toolTipText = error
        }
    }

    /**
     * Retrieves the current error message.
     *
     * @return The tooltip text of the error label.
     */
    fun getCurrentError(): String = errorLabel.toolTipText
}
