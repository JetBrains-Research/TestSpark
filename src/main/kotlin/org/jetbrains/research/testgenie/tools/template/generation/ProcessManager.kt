package org.jetbrains.research.testgenie.tools.template.generation

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import org.jetbrains.research.testgenie.data.FragmentToTestDada

interface ProcessManager {
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
