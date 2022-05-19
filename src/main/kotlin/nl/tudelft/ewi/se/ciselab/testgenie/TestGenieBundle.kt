package nl.tudelft.ewi.se.ciselab.testgenie

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

const val BUNDLE = "messages.TestGenie"

/**
 * Loads the EvoSuite messages from `messages.TestGenie` file in the `recourses` directory.
 */
object TestGenieBundle : DynamicBundle(BUNDLE) {
    @Nls
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        getMessage(key, *params)
}
