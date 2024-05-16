package org.jetbrains.research.testspark.tools.llm.generation.hf

import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform

class HuggingFacePlatform (
    override val name: String = "HuggingFace",
    override var token: String = "",
    override var model: String = "",
) : LLMPlatform
