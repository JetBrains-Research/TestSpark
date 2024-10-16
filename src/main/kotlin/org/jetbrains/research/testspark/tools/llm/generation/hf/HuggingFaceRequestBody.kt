package org.jetbrains.research.testspark.tools.llm.generation.hf

import org.jetbrains.research.testspark.core.data.ChatMessage

data class Parameters(
    val topProbability: Double,
    val temperature: Double,
)

data class HuggingFaceRequestBody(
    val messages: List<ChatMessage>,
    val parameters: Parameters,
)

/**
 * Sets LLM settings required to send inference requests to HF
 * For more info, see https://huggingface.co/docs/api-inference/en/detailed_parameters
 */
fun HuggingFaceRequestBody.toMap(): Map<String, Any> {
    return mapOf(
        "inputs" to this.messages.joinToString(separator = "\n") { it.content },
        // TODO: These parameters can be set by the user in the plugin's settings too.
        "parameters" to mapOf(
            "top_p" to this.parameters.topProbability,
            "temperature" to this.parameters.temperature,
            "min_length" to 4096,
            "max_length" to 8192,
            "max_new_tokens" to 250,
            "max_time" to 120.0,
            "return_full_text" to false,
        ),
    )
}
