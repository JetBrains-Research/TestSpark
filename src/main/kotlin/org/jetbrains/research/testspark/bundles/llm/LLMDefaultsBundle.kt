package org.jetbrains.research.testspark.bundles.llm

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `resources` directory.
 */
object LLMDefaultsBundle : DynamicBundle(LLMBundlePaths.defaults) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun get(@PropertyKey(resourceBundle = LLMBundlePaths.defaults) key: String): String = getMessage(key)
}
