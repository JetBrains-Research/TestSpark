package org.jetbrains.research.testspark.core.generation.llm.network.model

data class LlmParams(
    val model: String,
    val token: String,
    val systemPrompt: String? = null,
    val temperature: Float? = null,
    val topProbability: Float? = null,
)