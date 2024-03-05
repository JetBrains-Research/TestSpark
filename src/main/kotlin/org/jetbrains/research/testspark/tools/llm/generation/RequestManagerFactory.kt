package org.jetbrains.research.testspark.tools.llm.generation

import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.generation.grazie.GrazieRequestManager
import org.jetbrains.research.testspark.tools.llm.generation.openai.OpenAIRequestManager

interface RequestManagerFactory {
    fun getRequestManager(): RequestManager
}

class StandardRequestManagerFactory : RequestManagerFactory {
    override fun getRequestManager(): RequestManager {
        return when (val platform = SettingsArguments.currentLLMPlatformName()) {
            SettingsArguments.settingsState!!.openAIName -> OpenAIRequestManager()
            SettingsArguments.settingsState!!.grazieName -> GrazieRequestManager()
            else -> throw IllegalStateException("Unknown selected platform: $platform")
        }
    }
}
