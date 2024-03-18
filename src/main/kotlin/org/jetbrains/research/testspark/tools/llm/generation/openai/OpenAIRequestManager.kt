package org.jetbrains.research.testspark.tools.llm.generation.openai

import com.google.gson.GsonBuilder
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.HttpRequests.HttpStatusException
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.core.progress.MyProgressIndicator
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.generation.IJRequestManager
import org.jetbrains.research.testspark.tools.llm.generation.JUnitTestsAssembler
import java.net.HttpURLConnection

/**
 * This class represents a manager for making requests to the LLM (Large Language Model).
 */
class OpenAIRequestManager(project: Project) : IJRequestManager(project) {
    private val url = "https://api.openai.com/v1/chat/completions"

    private val httpRequest = HttpRequests.post(url, "application/json").tuner {
        it.setRequestProperty("Authorization", "Bearer $token")
    }

    private val llmErrorManager = LLMErrorManager()

    override fun send(
        prompt: String,
        indicator: MyProgressIndicator,
    ): Pair<SendResult, TestsAssembler> {
        // Prepare the chat
        val llmRequestBody = OpenAIRequestBody(SettingsArguments.getModel(), chatHistory)

        // Prepare the test assembler
        val testsAssembler = JUnitTestsAssembler(project, indicator)
        var sendResult = SendResult.OK

        try {
            httpRequest.connect {
                it.write(GsonBuilder().create().toJson(llmRequestBody))

                // check response
                when (val responseCode = (it.connection as HttpURLConnection).responseCode) {
                    HttpURLConnection.HTTP_OK -> testsAssembler.consume(it)
                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                        llmErrorManager.errorProcess(
                            TestSparkBundle.message("serverProblems"),
                            project,
                        )
                        sendResult = SendResult.OTHER
                    }

                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                        llmErrorManager.warningProcess(
                            TestSparkBundle.message("tooLongPrompt"),
                            project,
                        )
                        sendResult = SendResult.PROMPT_TOO_LONG
                    }

                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        llmErrorManager.errorProcess(
                            TestSparkBundle.message("wrongToken"),
                            project,
                        )
                        sendResult = SendResult.OTHER
                    }

                    else -> {
                        llmErrorManager.errorProcess(
                            llmErrorManager.createRequestErrorMessage(responseCode),
                            project,
                        )
                        sendResult = SendResult.OTHER
                    }
                }
            }
        } catch (e: HttpStatusException) {
            log.info("Error in sending request: ${e.message}")
        }

        return Pair(sendResult, testsAssembler)
    }
}
