package org.jetbrains.research.testspark

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

const val BUNDLE = "messages.TestSpark"

/**
 * Loads the EvoSuite messages from `messages/TestSpark.properties` file in the `resources` directory.
 */
object TestSparkBundle : DynamicBundle(BUNDLE) {

    /**
     * Gets the requested message.
     */
    @Nls
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        getMessage(key, *params)
}
