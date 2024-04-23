package org.jetbrains.research.testspark.tools.llm.generation.grazie

import org.jetbrains.research.testspark.bundles.DefaultsBundle
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform

class GraziePlatform(
    override val name: String = DefaultsBundle.defaultValue("grazieName"),
    override var token: String = DefaultsBundle.defaultValue("grazieToken"),
    override var model: String = DefaultsBundle.defaultValue("grazieModel"),
) : LLMPlatform
