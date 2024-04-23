package org.jetbrains.research.testspark.tools.llm.generation.openai

import org.jetbrains.research.testspark.bundles.DefaultsBundle
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform

class OpenAIPlatform(
    override val name: String = DefaultsBundle.defaultValue("openAIName"),
    override var token: String = DefaultsBundle.defaultValue("openAIToken"),
    override var model: String = DefaultsBundle.defaultValue("openAIModel"),
) : LLMPlatform
