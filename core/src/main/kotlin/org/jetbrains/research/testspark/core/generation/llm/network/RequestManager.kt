package org.jetbrains.research.testspark.core.generation.llm.network

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.data.ChatAssistantMessage
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.data.ChatUserMessage
import org.jetbrains.research.testspark.core.monitor.DefaultErrorMonitor
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.TestsAssembler

abstract class RequestManager(var token: String) {
    enum class SendResult {
        OK,
        PROMPT_TOO_LONG,
        OTHER,
    }

    val chatHistory = mutableListOf<ChatMessage>()

    protected val log = KotlinLogging.logger {}

    /**
     * Sends a request to LLM with the given prompt and returns the generated TestSuite.
     *
     * @param prompt the prompt to send to LLM
     * @param indicator the progress indicator to show progress during the request
     * @param packageName the name of the package for the generated TestSuite
     * @param isUserFeedback indicates if this request is a test generation request or a user feedback
     * @return the generated TestSuite, or null and prompt message
     */
    open fun request(
        language: SupportedLanguage,
        prompt: String,
        indicator: CustomProgressIndicator,
        packageName: String,
        testsAssembler: TestsAssembler,
        isUserFeedback: Boolean = false,
        errorMonitor: ErrorMonitor = DefaultErrorMonitor(), // The plugin for other IDEs can send LLM requests without passing an errorMonitor
    ): LLMResponse {
        // save the prompt in chat history
        chatHistory.add(ChatUserMessage(prompt))

        // Send Request to LLM
        log.info { "Sending Request..." }

        val sendResult = send(prompt, indicator, testsAssembler, errorMonitor)

        if (sendResult == SendResult.PROMPT_TOO_LONG) {
            return LLMResponse(ResponseErrorCode.PROMPT_TOO_LONG, null)
        }

        // we remove the user request because we don't store user's requests in chat history
        if (isUserFeedback) {
            chatHistory.removeLast()
        }

        return when (isUserFeedback) {
            true -> processUserFeedbackResponse(testsAssembler, packageName, language)
            false -> processResponse(testsAssembler, packageName, language)
        }
    }

    open fun processResponse(
        testsAssembler: TestsAssembler,
        packageName: String,
        language: SupportedLanguage,
    ): LLMResponse {
        // save the full response in the chat history
        val response = testsAssembler.getContent()

        log.info { "The full response: \n $response" }
        chatHistory.add(ChatAssistantMessage(response))

        // check if the response is empty
        if (response.isEmpty() || response.isBlank()) {
            return LLMResponse(ResponseErrorCode.EMPTY_LLM_RESPONSE, null)
        }

        val testSuiteGeneratedByLLM = testsAssembler.assembleTestSuite()

        return if (testSuiteGeneratedByLLM == null) {
            LLMResponse(ResponseErrorCode.TEST_SUITE_PARSING_FAILURE, null)
        } else {
            LLMResponse(ResponseErrorCode.OK, testSuiteGeneratedByLLM.reformat())
        }
    }

    abstract fun send(
        prompt: String,
        indicator: CustomProgressIndicator,
        testsAssembler: TestsAssembler,
        errorMonitor: ErrorMonitor = DefaultErrorMonitor(),
    ): SendResult

    open fun processUserFeedbackResponse(
        testsAssembler: TestsAssembler,
        packageName: String,
        language: SupportedLanguage,
    ): LLMResponse {
        val response = testsAssembler.getContent()

        log.info { "The full response: \n $response" }

        // check if the response is empty
        if (response.isEmpty() || response.isBlank()) {
            return LLMResponse(ResponseErrorCode.EMPTY_LLM_RESPONSE, null)
        }

        val testSuiteGeneratedByLLM = testsAssembler.assembleTestSuite()

        return if (testSuiteGeneratedByLLM == null) {
            LLMResponse(ResponseErrorCode.TEST_SUITE_PARSING_FAILURE, null)
        } else {
            LLMResponse(ResponseErrorCode.OK, testSuiteGeneratedByLLM)
        }
    }
}
