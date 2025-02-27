package org.jetbrains.research.testspark.core.generation.llm.network

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.data.ChatAssistantMessage
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.data.ChatUserMessage
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.core.error.Result
import org.jetbrains.research.testspark.core.monitor.DefaultErrorMonitor
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM

abstract class RequestManager(
    var token: String,
    val llmModel: String,
) {
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
    ): Result<TestSuiteGeneratedByLLM, TestSparkError> {
        // save the prompt in chat history
        chatHistory.add(ChatUserMessage(prompt))

        // Send Request to LLM
        log.info { "Sending Request..." }

        val sendResult = send(prompt, indicator, testsAssembler, errorMonitor)

        if (sendResult is Result.Failure) return sendResult

        // we remove the user request because we don't store user's requests in chat history
        if (isUserFeedback) {
            chatHistory.removeLast()
        }

        return processResponse(testsAssembler, packageName, language, isUserFeedback)
    }

    open fun processResponse(
        testsAssembler: TestsAssembler,
        packageName: String,
        language: SupportedLanguage,
        isUserFeedback: Boolean,
    ): Result<TestSuiteGeneratedByLLM, TestSparkError> {
        // save the full response in the chat history
        val response = testsAssembler.getContent()

        log.info { "The full response: \n $response" }
        if (!isUserFeedback) {
            chatHistory.add(ChatAssistantMessage(response))
        }

        // check if the response is empty
        if (response.isEmpty() || response.isBlank()) {
            return Result.Failure(error = LlmError.EmptyLlmResponse)
        }

        val testSuiteGeneratedByLLM = testsAssembler.assembleTestSuite()

        return if (testSuiteGeneratedByLLM == null) {
            Result.Failure(error = LlmError.TestSuiteParsingError)
        } else {
            Result.Success(data = testSuiteGeneratedByLLM.reformat())
        }
    }

    abstract fun send(
        prompt: String,
        indicator: CustomProgressIndicator,
        testsAssembler: TestsAssembler,
        errorMonitor: ErrorMonitor = DefaultErrorMonitor(),
    ): Result<Unit, TestSparkError>
}
