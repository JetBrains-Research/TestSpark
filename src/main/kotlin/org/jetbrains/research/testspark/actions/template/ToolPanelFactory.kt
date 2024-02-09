package org.jetbrains.research.testspark.actions.template

import org.jetbrains.research.testspark.actions.TestSparkAction
import javax.swing.JButton
import javax.swing.JPanel

interface ToolPanelFactory {
    fun getPanel(junit: TestSparkAction.TestSparkActionWindow.JUnit?): JPanel

    fun getBackButton(): JButton

    fun getOkButton(): JButton

    fun settingsStateUpdate()
}
