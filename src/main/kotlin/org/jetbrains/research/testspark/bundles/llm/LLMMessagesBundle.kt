package org.jetbrains.research.testspark.bundles.llm

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `resources` directory.
 */
object LLMMessagesBundle : DynamicBundle(LLMBundlePaths.messages) {

    /**
     * Gets the requested message.
     */
    @Nls
    fun message(@PropertyKey(resourceBundle = LLMBundlePaths.messages) key: String, vararg params: Any): String =
        getMessage(key, *params)
}
