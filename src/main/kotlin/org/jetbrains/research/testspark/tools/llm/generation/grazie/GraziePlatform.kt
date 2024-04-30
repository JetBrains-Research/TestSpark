package org.jetbrains.research.testspark.tools.llm.generation.grazie

import org.jetbrains.research.testspark.bundles.llm.LLMDefaultsBundle
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform

class GraziePlatform(
    override val name: String = LLMDefaultsBundle.get("grazieName"),
    override var token: String = LLMDefaultsBundle.get("grazieToken"),
    override var model: String = LLMDefaultsBundle.get("grazieModel"),
) : LLMPlatform
