package org.jetbrains.research.testspark.core.generation.prompt.configuration

data class PromptTemplates(
    val classPrompt: String,
    val methodPrompt: String,
    val linePrompt: String,
)