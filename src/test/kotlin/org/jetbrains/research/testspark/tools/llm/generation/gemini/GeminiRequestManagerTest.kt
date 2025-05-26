package org.jetbrains.research.testspark.tools.llm.generation.gemini

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.generation.llm.network.HttpRequestManager
import org.jetbrains.research.testspark.core.generation.llm.network.LlmProvider
import org.jetbrains.research.testspark.core.generation.llm.network.model.LlmParams
import org.jetbrains.research.testspark.helpers.LLMHelper
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

@EnabledIfEnvironmentVariable(named = "GOOGLE_API_KEY", matches = ".+")
class GeminiRequestManagerTest {
    private val requestManager = HttpRequestManager(llmProvider = LlmProvider.Gemini)
    private val llmParams =
        LlmParams(
            model = "gemini-1.5-flash",
            token = System.getenv("GOOGLE_API_KEY")!!,
        )

    @Test
    fun `test request manager implementation for Google Gemini`() =
        runTest {
            val prompt =
                """
                You are a Java tester.  Provide a test case that covers the following code snippet:

                ```java
                package com.example;
                public class Foo {
                  public int sign(int x) {
                    if (x > 0) return 1
                    if (x < 0) return -1
                    return 0
                  }
                }
                ```
                """.trimIndent()
            val chunks =
                requestManager
                    .sendRequest(
                        llmParams,
                        listOf(ChatMessage.createUserMessage(prompt)),
                    ).toList()
            assertAll(
                chunks.map { chunk ->
                    { assertTrue(chunk.isSuccess()) }
                },
            )
        }

    @Test
    fun `test the retrieved Gemini models`() {
        val models = LLMHelper.getGeminiModels(llmParams.token)
        assertTrue(models.isNotEmpty())
    }
}
