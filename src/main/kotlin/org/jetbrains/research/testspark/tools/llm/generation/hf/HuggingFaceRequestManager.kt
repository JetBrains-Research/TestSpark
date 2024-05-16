package org.jetbrains.research.testspark.tools.llm.generation.hf

import com.google.gson.GsonBuilder
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import org.jetbrains.research.testspark.bundles.llm.LLMMessagesBundle
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.generation.IJRequestManager
import org.jetbrains.research.testspark.tools.llm.generation.JUnitTestsAssembler
import java.net.HttpURLConnection

/**
 * A class to manage requests sent to large language models hosted on HuggingFace
 */
class HuggingFaceRequestManager(project: Project) : IJRequestManager(project) {
    private val url = "https://api-inference.huggingface.co/models/meta-llama/"
    private val systemPrompt = "You are a helpful and honest code and programming assistant." +
            " Please, respond concisely and truthfully."
    // TODO: The user should be able to change these numbers in the plugin's settings
    private val topProbability = 0.9
    private val temperature = 0.9

    private val llmErrorManager = LLMErrorManager()

    override fun send(
        prompt: String,
        indicator: CustomProgressIndicator,
        testsAssembler: TestsAssembler,
        errorMonitor: ErrorMonitor
    ): SendResult {
        val httpRequest = HttpRequests.post(url + SettingsArguments(project).getModel(),
            "application/json").tuner {
            it.setRequestProperty("Authorization", "Bearer $token")
        }

        // Add system prompt
        if (chatHistory.size == 1) {
            chatHistory[0] = ChatMessage(chatHistory[0].role, formatPrompt(systemPrompt, chatHistory[0].content))
        }

        val llmRequestBody = HuggingFaceRequestBody(chatHistory, Parameters(topProbability, temperature)).toMap()
        var sendResult = SendResult.OK

        httpRequest.connect {
            it.write(GsonBuilder().create().toJson(llmRequestBody))
            when (val responseCode = (it.connection as HttpURLConnection).responseCode) {
                HttpURLConnection.HTTP_OK -> (testsAssembler as JUnitTestsAssembler).consumeHFRequest(it)
                HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                    llmErrorManager.errorProcess(
                        LLMMessagesBundle.get("serverProblems"),
                        project,
                        errorMonitor,
                    )
                    sendResult = SendResult.OTHER
                }
            }
        }

        return sendResult;
    }

    /**
     * Creates the required prompt for Llama models. For more details see:
     * https://huggingface.co/blog/llama2#how-to-prompt-llama-2
     */
    private fun formatPrompt(systemPrompt: String, userMessage: String): String {
        return "<s>[INST] <<SYS>> $systemPrompt <</SYS>> $userMessage [/INST]"
    }
}