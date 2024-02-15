package org.jetbrains.research.testspark.actions.template

import javax.swing.JButton
import javax.swing.JPanel

interface ToolPanelFactory {
    fun getTitlePanel(): JPanel

    fun getMiddlePanel(): JPanel

    fun getBottomPanel(): JPanel

    fun getBackButton(): JButton

    fun getFinishedButton(): JButton

    fun applyUpdates()
}
