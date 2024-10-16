package org.jetbrains.research.testspark.bundles.llm

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `recourses` directory.
 */
object LLMLabelsBundle : DynamicBundle(LLMBundlePaths.labels) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun get(@PropertyKey(resourceBundle = LLMBundlePaths.labels) key: String): String = getMessage(key)
}
