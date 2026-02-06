package org.jetbrains.research.testspark.bundles.evosuite

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `resources` directory.
 */
object EvoSuiteMessagesBundle : DynamicBundle(EvoSuiteBundlePaths.MESSAGES) {
    /**
     * Gets the requested message.
     */
    @Nls
    fun get(
        @PropertyKey(resourceBundle = EvoSuiteBundlePaths.MESSAGES) key: String,
        vararg params: Any,
    ): String = getMessage(key, *params)
}
