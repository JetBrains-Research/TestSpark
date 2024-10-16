package org.jetbrains.research.testspark.bundles.plugin

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `resources` directory.
 */
object PluginSettingsBundle : DynamicBundle(PluginBundlePaths.settings) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun get(@PropertyKey(resourceBundle = PluginBundlePaths.settings) key: String): String = getMessage(key)
}
