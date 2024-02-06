package org.jetbrains.research.testspark.tools.llm.generation.openai

import com.google.gson.annotations.SerializedName

data class OpenAIChoice(
    val index: Int,
    val delta: Delta,
    @SerializedName("finish_reason")
    val finishedReason: String,
)

data class Delta(val role: String?, val content: String)
