package org.jetbrains.research.testspark.actions.template

import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import javax.swing.JButton
import javax.swing.JPanel

interface PanelFactory {
    val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

    fun getTitlePanel(): JPanel

    fun getMiddlePanel(): JPanel

    fun getBottomPanel(): JPanel

    fun getBackButton(): JButton

    fun getFinishedButton(): JButton

    fun applyUpdates()
}
