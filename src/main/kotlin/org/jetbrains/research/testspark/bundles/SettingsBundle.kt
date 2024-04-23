package org.jetbrains.research.testspark.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the tooltip texts from `defaults/Settings.properties` file in the `resources` directory.
 */
object SettingsBundle : DynamicBundle(BundlePaths.SETTINGS) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun defaultValue(@PropertyKey(resourceBundle = BundlePaths.SETTINGS) key: String): String = getMessage(key)
}
