package org.jetbrains.research.testspark.tools.template.generation

import org.jetbrains.research.testspark.core.progress.MyProgressIndicator
import org.jetbrains.research.testspark.data.FragmentToTestData

/**
 * An interface representing a process manager.
 */
interface ProcessManager {
    /**
     * Runs the test generator for a given code fragment.
     *
     * @param indicator The progress indicator to track the progress of the test generation.
     * @param codeType The type of the code fragment to test.
     * @param packageName The package name of the code fragment.
     */
    fun runTestGenerator(
        indicator: MyProgressIndicator,
        codeType: FragmentToTestData,
        packageName: String,
    )
}
