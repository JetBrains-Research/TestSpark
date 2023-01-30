package org.jetbrains.research.testgenie

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

const val BUNDLE = "messages.TestGenie"

/**
 * Loads the EvoSuite messages from `messages/TestGenie.properties` file in the `resources` directory.
 */
object TestGenieBundle : DynamicBundle(BUNDLE) {

    /**
     * Gets the requested message.
     */
    @Nls
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        getMessage(key, *params)
}
