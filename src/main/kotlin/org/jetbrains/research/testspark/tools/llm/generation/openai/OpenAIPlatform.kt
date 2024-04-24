package org.jetbrains.research.testspark.tools.llm.generation.openai

import org.jetbrains.research.testspark.bundles.llm.LLMDefaultsBundle
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform

class OpenAIPlatform(
    override val name: String = LLMDefaultsBundle.defaultValue("openAIName"),
    override var token: String = LLMDefaultsBundle.defaultValue("openAIToken"),
    override var model: String = LLMDefaultsBundle.defaultValue("openAIModel"),
) : LLMPlatform
