package org.jetbrains.research.testgenie.tools.template.generation

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import org.jetbrains.research.testgenie.data.FragmentToTestDada

/**
 * An interface representing a process manager.
 */
interface ProcessManager {
    /**
     * Runs the test generator for a given code fragment.
     *
     * @param indicator The progress indicator to track the progress of the test generation.
     * @param codeType The type of the code fragment to test.
     * @param projectClassPath The classpath of the project that contains the code fragment.
     * @param resultPath The path where the generated tests will be saved.
     * @param serializeResultPath The path where the serialized test result will be saved.
     * @param packageName The package name of the code fragment.
     * @param cutModule The module that contains the code fragment.
     * @param classFQN The fully qualified name of the class that contains the code fragment.
     * @param fileUrl The URL of the file that contains the code fragment.
     * @param testResultName The name of the test result.
     * @param baseDir The base directory of the project.
     * @param log The logger to log the progress and errors.
     * @param modificationStamp The modification stamp of the code fragment file.
     */
    fun runTestGenerator(
        indicator: ProgressIndicator,
        codeType: FragmentToTestDada,
        projectClassPath: String,
        resultPath: String,
        serializeResultPath: String,
        packageName: String,
        cutModule: Module,
        classFQN: String,
        fileUrl: String,
        testResultName: String,
        baseDir: String,
        log: Logger,
        modificationStamp: Long,
    )
}
