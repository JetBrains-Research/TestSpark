package org.jetbrains.research.testspark.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class TestSparkAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = MyDialogWrapper()
        dialog.showAndGet()
    }

    class MyDialogWrapper : DialogWrapper(true) {
        init {
            init()
            title = "TestSpark"
        }

        override fun createCenterPanel(): JComponent {
            val panel = panel {
                row {
                    label("Select the test generator:")
                    comboBox(DefaultComboBoxModel(arrayOf("LLM", "EvoSuite")))
                }
                row {
                    label("Scope:")
                    comboBox(DefaultComboBoxModel(arrayOf("Class", "Method", "Line")))
                }
            }
            return panel
        }
    }
}
