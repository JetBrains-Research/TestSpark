package org.jetbrains.research.testspark.tools.llm.generation.openai

import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform

class OpenAIPlatform(
    override val name: String = TestSparkDefaultsBundle.defaultValue("openAIName"),
    override var token: String = TestSparkDefaultsBundle.defaultValue("openAIToken"),
    override var model: String = TestSparkDefaultsBundle.defaultValue("openAIModel"),
) : LLMPlatform
