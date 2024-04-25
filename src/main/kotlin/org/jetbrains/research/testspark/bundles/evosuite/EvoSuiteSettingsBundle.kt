package org.jetbrains.research.testspark.bundles.evosuite

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `resources` directory.
 */
object EvoSuiteSettingsBundle : DynamicBundle(EvoSuiteBundlePaths.settings) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun get(@PropertyKey(resourceBundle = EvoSuiteBundlePaths.settings) key: String): String = getMessage(key)
}
