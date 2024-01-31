package org.jetbrains.research.testspark.tools.llm.generation

import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.tools.llm.SettingsArguments

interface RequestManagerFactory {
    fun getRequestManager(): RequestManager
}

class StandardRequestManagerFactory : RequestManagerFactory {
    override fun getRequestManager(): RequestManager {
        return when (val platform = SettingsArguments.currentLLMPlatformName()) {
            TestSparkLabelsBundle.defaultValue("grazie") -> GrazieRequestManager()
            TestSparkLabelsBundle.defaultValue("openAI") -> OpenAIRequestManager()
            else -> throw IllegalStateException("Unknown selected platform: $platform")
        }
    }
}
