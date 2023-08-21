package org.jetbrains.research.testspark.tools.llm.generation

import org.jetbrains.research.testspark.tools.llm.SettingsArguments

interface RequestManagerFactory {
    fun getRequestManager(): RequestManager
}


class StandardRequestManagerFactory: RequestManagerFactory {
    override fun getRequestManager(): RequestManager {
        return when(val platform = SettingsArguments.llmPlatform()) {
            "Grazie" -> GrazieRequestManager()
            "OpenAI" -> OpenAIRequestManager()
            else -> throw IllegalStateException("Unknown selected platform: $platform")
        }
    }
}