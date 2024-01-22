package org.jetbrains.research.testspark.tools.llm.generation

import com.google.gson.GsonBuilder
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.HttpRequests.HttpStatusException
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import java.net.HttpURLConnection

/**
 * This class represents a manager for making requests to the LLM (Large Language Model).
 */
class OpenAIRequestManager : RequestManager() {

    private val url = "https://api.openai.com/v1/chat/completions"
    private val model = SettingsArguments.model()

    private val httpRequest = HttpRequests.post(url, "application/json").tuner {
        it.setRequestProperty("Authorization", "Bearer $token")
    }

    override fun send(
        prompt: String,
        indicator: ProgressIndicator,
        project: Project,
        llmErrorManager: LLMErrorManager,
    ): TestsAssembler {
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
            log.info("Error in sending request: ${e.message}")
        }

        return testsAssembler
    }
}
