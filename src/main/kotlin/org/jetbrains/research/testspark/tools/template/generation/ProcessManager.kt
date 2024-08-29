package org.jetbrains.research.testspark.tools.template.generation

import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.data.ProjectContext
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.tools.TestsExecutionResultManager

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
        indicator: CustomProgressIndicator,
        codeType: FragmentToTestData,
        packageName: String,
        projectContext: ProjectContext,
        generatedTestsData: TestGenerationData,
        errorMonitor: ErrorMonitor,
        testsExecutionResultManager: TestsExecutionResultManager,
    ): UIContext?
}
