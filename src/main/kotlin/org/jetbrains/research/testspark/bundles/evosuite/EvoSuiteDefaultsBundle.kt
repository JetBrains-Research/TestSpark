package org.jetbrains.research.testspark.bundles.evosuite

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `resources` directory.
 */
object EvoSuiteDefaultsBundle : DynamicBundle(EvoSuiteBundlePaths.defaults) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun get(@PropertyKey(resourceBundle = EvoSuiteBundlePaths.defaults) key: String): String = getMessage(key)
}
