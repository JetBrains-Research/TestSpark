package org.jetbrains.research.testspark.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the EvoSuite messages from `messages/TestSpark.properties` file in the `resources` directory.
 */
object TestSparkBundle : DynamicBundle(BundlePaths.MESSAGES) {

    /**
     * Gets the requested message.
     */
    @Nls
    fun message(@PropertyKey(resourceBundle = BundlePaths.MESSAGES) key: String, vararg params: Any): String =
        getMessage(key, *params)
}
