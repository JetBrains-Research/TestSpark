package org.jetbrains.research.testspark.tools.llm.generation

interface LLMPlatform {
    val name: String
    var token: String
    var model: String

    fun getModels(providedToken: String): Array<String>
}
