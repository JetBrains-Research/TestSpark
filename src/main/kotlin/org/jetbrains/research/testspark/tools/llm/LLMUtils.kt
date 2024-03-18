package org.jetbrains.research.testspark.tools.llm

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import java.util.*

/**
 * Checks if the token is set.
 *
 * @return True if the token is set, false otherwise.
 */
fun isCorrectToken(project:Project): Boolean {
    if (!SettingsArguments.isTokenSet()) {
        LLMErrorManager().errorProcess(TestSparkBundle.message("missingToken"), project)
        return false
    }
    return true
}


/**
 * Returns the generated class name for a given test case.
 *
 * @param testCaseName The test case name.
 * @return The generated class name as a string.
 */
fun getClassWithTestCaseName(testCaseName: String): String {
    val className = testCaseName.replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            it.toString()
        }
    }
    return "Generated$className"
}