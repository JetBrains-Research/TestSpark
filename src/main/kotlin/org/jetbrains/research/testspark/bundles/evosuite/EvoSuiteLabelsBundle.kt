package org.jetbrains.research.testspark.bundles.evosuite

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `recourses` directory.
 */
object EvoSuiteLabelsBundle : DynamicBundle(EvoSuiteBundlePaths.LABELS) {
    /**
     * Gets the requested default value.
     */
    @Nls
    fun get(
        @PropertyKey(resourceBundle = EvoSuiteBundlePaths.LABELS) key: String,
    ): String = getMessage(key)
}
