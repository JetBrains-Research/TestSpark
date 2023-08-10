package org.jetbrains.research.testgenie.tools.llm.generation

import com.google.gson.GsonBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.HttpRequests.HttpStatusException
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.tools.llm.SettingsArguments
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM
import java.net.HttpURLConnection

/**
 * This class represents a manager for making requests to the LLM (Live Learning Model).
 */
class LLMRequestManager {
    private val url = "https://api.openai.com/v1/chat/completions"

    private val llmToken = SettingsArguments.llmUserToken()
    private val model = SettingsArguments.model()

    private val log: Logger = Logger.getInstance(this.javaClass)

    private val httpRequest = HttpRequests.post(url, "application/json").tuner {
        it.setRequestProperty("Authorization", "Bearer $llmToken")
    }

    private val chatHistory = mutableListOf<OpenAIMessage>()

    /**
     * Sends a request to LLM with the given prompt and returns the generated TestSuite.
     *
     * @param prompt the prompt to send to LLM
     * @param indicator the progress indicator to show progress during the request
     * @param packageName the name of the package for the generated TestSuite
     * @param project the project associated with the request
     * @param llmErrorManager the error manager to handle errors during the request
     * @return the generated TestSuite, or null and prompt message
     */
    fun request(prompt: String, indicator: ProgressIndicator, packageName: String, project: Project, llmErrorManager: LLMErrorManager): Pair<String, TestSuiteGeneratedByLLM?> {
        // Prepare the chat
        val llmRequestBody = buildRequestBody(prompt)

        // Prepare the test assembler
        val testsAssembler = TestsAssembler(project, indicator)

        // Send Request to LLM
        log.info("Sending Request ...")
        try {
            httpRequest.connect {
                it.write(GsonBuilder().create().toJson(llmRequestBody))

                // check response
                when (val responseCode = (it.connection as HttpURLConnection).responseCode) {
                    HttpURLConnection.HTTP_OK -> testsAssembler.receiveResponse(it)
                    HttpURLConnection.HTTP_INTERNAL_ERROR -> llmErrorManager.errorProcess(
                        TestGenieBundle.message("serverProblems"),
                        project,
                    )

                    HttpURLConnection.HTTP_BAD_REQUEST -> llmErrorManager.errorProcess(
                        TestGenieBundle.message("tooLongPrompt"),
                        project,
                    )

                    HttpURLConnection.HTTP_UNAUTHORIZED -> llmErrorManager.errorProcess(
                        TestGenieBundle.message("wrongToken"),
                        project,
                    )

                    else -> llmErrorManager.errorProcess(
                        llmErrorManager.createRequestErrorMessage(responseCode),
                        project,
                    )
                }
            }
        } catch (e: HttpStatusException) {
            return Pair("", null)
        }
        // save the full response in the chat history
        val response = testsAssembler.rawText
        log.debug("The full response: \n $response")
        chatHistory.add(OpenAIMessage("assistant", response))

        // check if response is empty
        if (response.isEmpty() || response.isBlank()) return Pair("You have provided an empty answer! Please answer my previous question with the same formats", null)

        val testSuiteGeneratedByLLM = testsAssembler.returnTestSuite(packageName) ?: return Pair("The provided code is not parsable. Please give the correct code", null)

        return Pair("", testSuiteGeneratedByLLM.reformat())
    }

    /**
     * Builds a new LLMChat instance using the given prompt.
     * Adds the prompt to the chat history and then constructs the LLMChat object with the chat history.
     *
     * @param prompt The prompt for the user.
     * @return The newly created LLMChat object.
     */
    private fun buildRequestBody(prompt: String): OpenAIRequestBody {
        // add new prompt to chat history
        chatHistory.add(OpenAIMessage("user", prompt))

        return OpenAIRequestBody(model, chatHistory)
    }
}
