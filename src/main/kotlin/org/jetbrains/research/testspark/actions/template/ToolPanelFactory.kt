package org.jetbrains.research.testspark.actions.template

import org.jetbrains.research.testspark.data.JUnitVersion
import javax.swing.JButton
import javax.swing.JPanel

interface ToolPanelFactory {
    fun getPanel(junit: JUnitVersion?): JPanel

    fun getBackButton(): JButton

    fun getOkButton(): JButton

    fun settingsStateUpdate()
}
