package org.jetbrains.research.testspark.tools.llm.generation

import com.google.gson.GsonBuilder
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.HttpRequests.HttpStatusException
import org.jetbrains.research.testspark.TestSparkBundle
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.test.TestSuiteGeneratedByLLM
import java.net.HttpURLConnection

/**
 * This class represents a manager for making requests to the LLM (Live Learning Model).
 */
class OpenAIRequestManager : RequestManager() {

    private val url = "https://api.openai.com/v1/chat/completions"
    private val model = SettingsArguments.model()

    private val httpRequest = HttpRequests.post(url, "application/json").tuner {
        it.setRequestProperty("Authorization", "Bearer $token")
    }

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
    override fun request(prompt: String, indicator: ProgressIndicator, packageName: String, project: Project, llmErrorManager: LLMErrorManager): Pair<String, TestSuiteGeneratedByLLM?> {
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
                        TestSparkBundle.message("serverProblems"),
                        project,
                    )

                    HttpURLConnection.HTTP_BAD_REQUEST -> llmErrorManager.errorProcess(
                        TestSparkBundle.message("tooLongPrompt"),
                        project,
                    )

                    HttpURLConnection.HTTP_UNAUTHORIZED -> llmErrorManager.errorProcess(
                        TestSparkBundle.message("wrongToken"),
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
        return processResponse(testsAssembler, packageName)
    }

    /**
     * Builds a new OpenAI request body instance using the given prompt.
     * Adds the prompt to the chat history and then constructs the OpenAIRequestBody using the chatHistory and model
     *
     * @param prompt The prompt for the user.
     * @return The newly created OpenAIRequestBody object.
     */
    private fun buildRequestBody(prompt: String): OpenAIRequestBody {
        // add new prompt to chat history
        chatHistory.add(ChatMessage("user", prompt))

        return OpenAIRequestBody(model, chatHistory)
    }
}
