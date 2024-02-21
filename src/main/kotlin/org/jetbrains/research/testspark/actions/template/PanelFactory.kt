package org.jetbrains.research.testspark.actions.template

import org.jetbrains.research.testspark.data.JUnitVersion
import javax.swing.JButton
import javax.swing.JPanel

interface PanelFactory {
    fun getTitlePanel(): JPanel

    fun getMiddlePanel(junit: JUnitVersion?): JPanel

    fun getBottomPanel(): JPanel

    fun getBackButton(): JButton

    fun getFinishedButton(): JButton

    fun applyUpdates()
}
