package org.jetbrains.research.testspark.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the default values from `defaults/TestSpark.properties` file in the `resources` directory.
 */
object TestSparkDefaultsBundle : DynamicBundle(BundlePaths.DEFAULTS) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun defaultValue(@PropertyKey(resourceBundle = BundlePaths.DEFAULTS) key: String): String = getMessage(key)
}
