package org.jetbrains.research.testspark.tools.llm.generation.grazie

import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform

class GraziePlatform(
    override val name: String = TestSparkDefaultsBundle.defaultValue("grazie"),
    override var token: String = TestSparkDefaultsBundle.defaultValue("grazieToken"),
    override var model: String = TestSparkDefaultsBundle.defaultValue("grazieModel"),
) : LLMPlatform
