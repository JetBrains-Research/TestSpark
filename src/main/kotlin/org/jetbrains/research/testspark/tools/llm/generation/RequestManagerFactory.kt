package org.jetbrains.research.testspark.tools.llm.generation

import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.generation.grazie.GrazieRequestManager
import org.jetbrains.research.testspark.tools.llm.generation.openai.OpenAIRequestManager

interface RequestManagerFactory {
    fun getRequestManager(): RequestManager
}

class StandardRequestManagerFactory : RequestManagerFactory {
    private val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

    override fun getRequestManager(): RequestManager {
        return when (val platform = SettingsArguments.currentLLMPlatformName()) {
            settingsState.openAIName -> OpenAIRequestManager()
            settingsState.grazieName -> GrazieRequestManager()
            else -> throw IllegalStateException("Unknown selected platform: $platform")
        }
    }
}
