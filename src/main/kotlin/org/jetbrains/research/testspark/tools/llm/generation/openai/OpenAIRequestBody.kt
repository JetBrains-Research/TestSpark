package org.jetbrains.research.testspark.tools.llm.generation.openai

/**
 * Adheres the naming of fields for OpenAI chat completion API and checks the correctness of a `role`.
 * <br/>
 * Use this class as a carrier of messages that should be sent to OpenAI API.
 */
data class OpenAIChatMessage(val role: String, val content: String) {
    private companion object {
        /**
         * The API strictly defines the set of roles.
         * The `function` role is omitted because it is already deprecated.
         *
         * See: https://platform.openai.com/docs/api-reference/chat/create
         */
        val supportedRoles = listOf("user", "assistant", "system", "tool")
    }

    init {
        if (!supportedRoles.contains(role)) {
            throw IllegalArgumentException("'$role' is not supported ${OpenAIChatMessage::class}. Available roles are: ${(supportedRoles.joinToString(", ") { "'$it'" })}")
        }
    }
}

data class OpenAIRequestBody(
    val model: String,
    val messages: List<OpenAIChatMessage>,
    val stream: Boolean = true,
)
