package org.jetbrains.research.testspark.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

const val DEFAULTS_BUNDLE = "defaults.TestSpark"

/**
 * Loads the default values from `defaults/TestSpark.properties` file in the `resources` directory.
 */
object TestSparkDefaultsBundle : DynamicBundle(DEFAULTS_BUNDLE) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun defaultValue(@PropertyKey(resourceBundle = DEFAULTS_BUNDLE) key: String): String = getMessage(key)
}
