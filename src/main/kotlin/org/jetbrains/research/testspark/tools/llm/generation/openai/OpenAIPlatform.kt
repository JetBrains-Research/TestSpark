package org.jetbrains.research.testspark.tools.llm.generation.openai

import org.jetbrains.research.testspark.bundles.llm.LLMDefaultsBundle
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform

class OpenAIPlatform(
    override val name: String = LLMDefaultsBundle.get("openAIName"),
    override var token: String = LLMDefaultsBundle.get("openAIToken"),
    override var model: String = LLMDefaultsBundle.get("openAIModel"),
) : LLMPlatform
