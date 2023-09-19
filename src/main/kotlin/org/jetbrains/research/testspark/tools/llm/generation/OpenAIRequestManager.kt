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
    override fun request(prompt: String,
                         indicator: ProgressIndicator,
                         packageName: String,
                         project: Project,
                         llmErrorManager: LLMErrorManager,
                         isUserFeedback: Boolean
    ): Pair<String, TestSuiteGeneratedByLLM?> {

        // save the prompt in chat history if it is not user feedback

        chatHistory.add(ChatMessage("user", prompt))

        // Send Request to LLM
        log.info("Sending Request ...")
        val testsAssembler = send(prompt, indicator,project,llmErrorManager)

        // we remove the user request because we dont users requests in chat history
        if (isUserFeedback)
            chatHistory.removeLast()

        return when(isUserFeedback){
            true -> processUserFeedbackResponse(testsAssembler, packageName)
            false -> processResponse(testsAssembler, packageName)
        }
    }
    override fun send(prompt: String,
                      indicator: ProgressIndicator,
                      project: Project,
                      llmErrorManager: LLMErrorManager): TestsAssembler {

        // Prepare the chat
        val llmRequestBody = OpenAIRequestBody(model, chatHistory)

        // Prepare the test assembler
        val testsAssembler = TestsAssembler(project, indicator)

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
            log.error("Error in sending request: ${e.message}")
        }

        return testsAssembler
    }
}
