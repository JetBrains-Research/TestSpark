package org.jetbrains.research.testspark.tools.llm.generation.grazie

import org.jetbrains.research.testspark.bundles.llm.LLMDefaultsBundle
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform

class GraziePlatform(
    override val name: String = LLMDefaultsBundle.defaultValue("grazieName"),
    override var token: String = LLMDefaultsBundle.defaultValue("grazieToken"),
    override var model: String = LLMDefaultsBundle.defaultValue("grazieModel"),
) : LLMPlatform
