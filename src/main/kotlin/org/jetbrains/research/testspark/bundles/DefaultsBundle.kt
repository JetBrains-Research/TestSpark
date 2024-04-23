package org.jetbrains.research.testspark.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the default values from `defaults/Messages.properties` file in the `resources` directory.
 */
object DefaultsBundle : DynamicBundle(BundlePaths.DEFAULTS) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun defaultValue(@PropertyKey(resourceBundle = BundlePaths.DEFAULTS) key: String): String = getMessage(key)
}
