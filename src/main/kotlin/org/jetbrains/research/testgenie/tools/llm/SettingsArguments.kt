package org.jetbrains.research.testgenie.tools.llm

import org.jetbrains.research.testgenie.services.SettingsApplicationService

class SettingsArguments {
    companion object {
        val settingsState = SettingsApplicationService.getInstance().state
        fun grazieUserToken(): String = settingsState!!.grazieUserToken
        fun maxLLMRequest(): Int = settingsState!!.maxLLMRequest
        fun maxInputParamsDepth(): Int = settingsState!!.maxInputParamsDepth
        fun maxPolyDepth(): Int = settingsState!!.maxPolyDepth
        fun isTokenSet(): Boolean = settingsState!!.grazieUserToken.isNotEmpty()
    }
}
