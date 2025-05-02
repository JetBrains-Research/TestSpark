package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.generation.llm.ChatSessionManager
import org.jetbrains.research.testspark.core.generation.llm.network.HttpRequestManager
import org.jetbrains.research.testspark.core.generation.llm.network.LlmProvider
import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager
import org.jetbrains.research.testspark.core.generation.llm.network.model.LlmParams
import org.jetbrains.research.testspark.helpers.LLMHelper.LlamaModels
import org.jetbrains.research.testspark.settings.llm.LLMSettingsState.DefaultLLMSettingsState
import org.jetbrains.research.testspark.tools.llm.LlmSettingsArguments
import org.jetbrains.research.testspark.tools.llm.generation.grazie.GrazieRequestManager

object ChatSessionManagerFactory {
    fun getChatSessionManager(project: Project): ChatSessionManager = ChatSessionManager(
        requestManager = getRequestManager(project),
        llmParams = LlmParams(
            model = LlmSettingsArguments(project).getModel(),
            token = LlmSettingsArguments(project).getToken(),
        )
    )

    private fun getRequestManager(project: Project): RequestManager {
        val model = LlmSettingsArguments(project).getModel()
        return when (val platform = LlmSettingsArguments(project).currentLLMPlatformName()) {
            DefaultLLMSettingsState.grazieName -> GrazieRequestManager()
            DefaultLLMSettingsState.openAIName -> HttpRequestManager(LlmProvider.OpenAI)
            DefaultLLMSettingsState.huggingFaceName -> when {
                model in LlamaModels -> HttpRequestManager(LlmProvider.Llama)
                else -> throw IllegalStateException("Unsupported model: $model")
            }
            DefaultLLMSettingsState.geminiName -> HttpRequestManager(LlmProvider.Gemini)
            else -> throw IllegalStateException("Unknown selected platform: $platform")
        }
    }
}