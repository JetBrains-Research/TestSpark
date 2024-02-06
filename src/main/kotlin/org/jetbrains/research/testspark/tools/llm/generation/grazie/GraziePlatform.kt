package org.jetbrains.research.testspark.tools.llm.generation.grazie

import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform

class GraziePlatform(
    override val name: String = TestSparkDefaultsBundle.defaultValue("grazie"),
    override var token: String = TestSparkDefaultsBundle.defaultValue("grazieToken"),
    override var model: String = TestSparkDefaultsBundle.defaultValue("grazieModel"),
) : LLMPlatform {
    private fun loadGrazieInfo(): GrazieInfo? {
        val className = "org.jetbrains.research.grazie.Info"
        return try {
            Class.forName(className).getDeclaredConstructor().newInstance() as GrazieInfo
        } catch (e: ClassNotFoundException) {
            null
        }
    }

    override fun getModels(providedToken: String): Array<String> = loadGrazieInfo()?.availableProfiles()?.toTypedArray() ?: arrayOf("")
}
