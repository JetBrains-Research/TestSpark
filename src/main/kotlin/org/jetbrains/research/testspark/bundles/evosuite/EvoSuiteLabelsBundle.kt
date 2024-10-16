package org.jetbrains.research.testspark.bundles.evosuite

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `recourses` directory.
 */
object EvoSuiteLabelsBundle : DynamicBundle(EvoSuiteBundlePaths.labels) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun get(@PropertyKey(resourceBundle = EvoSuiteBundlePaths.labels) key: String): String = getMessage(key)
}
