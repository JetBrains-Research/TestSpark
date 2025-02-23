package org.jetbrains.research.testspark.tools.llm.generation.gemini

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import org.jetbrains.research.testspark.core.data.LlmModuleType
import org.jetbrains.research.testspark.core.data.TestSparkModule
import org.jetbrains.research.testspark.core.error.HttpError
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.tools.ToolUtils
import org.jetbrains.research.testspark.tools.llm.generation.TestSparkRequestManager
import java.net.HttpURLConnection
import java.net.URLConnection

class GeminiRequestManager(project: Project) : TestSparkRequestManager(project) {

    private val gson = GsonBuilder().create()

    override val url: String
        get() {
            val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
            return "$baseUrl$llmModel:generateContent?key=$token"
        }

    override val moduleType: LlmModuleType = LlmModuleType.Gemini

    override fun tuneRequest(connection: URLConnection) = Unit

    override fun assembleRequestBodyJson(): String {
        val messages = chatHistory.map { GeminiChatMessage(it.content) }
        val geminiRequest = GeminiRequest(listOf(GeminiRequestBody(messages)))
        return gson.toJson(geminiRequest)
    }

    override fun mapHttpCodeToError(httpCode: Int): TestSparkError = when(httpCode) {
        HttpURLConnection.HTTP_BAD_REQUEST -> LlmError.PromptTooLong
        else -> HttpError(httpCode = httpCode, module = TestSparkModule.LLM(LlmModuleType.Gemini))
    }

    override fun assembleResponse(
        httpRequest: HttpRequests.Request,
        testsAssembler: TestsAssembler,
        indicator: CustomProgressIndicator,
        errorMonitor: ErrorMonitor,
    ) {
        while (true) {
            if (ToolUtils.isProcessCanceled(errorMonitor, indicator)) return

            val text = httpRequest.reader.readText()
            val result =
                gson.fromJson(
                    JsonParser.parseString(text)
                        .asJsonObject["candidates"]
                        .asJsonArray[0].asJsonObject,
                    GeminiReply::class.java,
                )

            testsAssembler.consume(result.content.parts[0].text)

            if (result.finishReason == "STOP") break
        }

        log.debug { testsAssembler.getContent() }
    }
}
