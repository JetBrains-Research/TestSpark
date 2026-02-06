package org.jetbrains.research.testspark.bundles.llm

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `resources` directory.
 */
object LLMMessagesBundle : DynamicBundle(LLMBundlePaths.MESSAGES) {
    /**
     * Gets the requested message.
     */
    @Nls
    fun get(
        @PropertyKey(resourceBundle = LLMBundlePaths.MESSAGES) key: String,
        vararg params: Any,
    ): String = getMessage(key, *params)
}
