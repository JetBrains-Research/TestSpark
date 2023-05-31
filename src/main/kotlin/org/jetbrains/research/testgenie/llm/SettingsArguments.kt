package org.jetbrains.research.testgenie.llm

import org.jetbrains.research.testgenie.services.SettingsApplicationService

class SettingsArguments {
    companion object {
        val settingsState = SettingsApplicationService.getInstance().state
        fun grazieUserToken() : String = settingsState!!.grazieUserToken
        fun isTokenSet(): Boolean = settingsState!!.grazieUserToken.isNotEmpty()
    }

}