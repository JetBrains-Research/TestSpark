package org.jetbrains.research.testspark.bundles.plugin

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `resources` directory.
 */
object PluginMessagesBundle : DynamicBundle(PluginBundlePaths.messages) {

    /**
     * Gets the requested message.
     */
    @Nls
    fun get(@PropertyKey(resourceBundle = PluginBundlePaths.messages) key: String, vararg params: Any): String =
        getMessage(key, *params)
}
