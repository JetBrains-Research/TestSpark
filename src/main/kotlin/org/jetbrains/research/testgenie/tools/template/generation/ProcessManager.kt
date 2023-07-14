package org.jetbrains.research.testgenie.tools.template.generation

import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import org.jetbrains.research.testgenie.data.CodeType

interface ProcessManager {
    fun runTestGenerator(
        indicator: ProgressIndicator,
        codeType: CodeType,
        resultPath: String,
        packageName: String,
        cutModule: Module,
        classFQN: String,
        fileUrl: String,
        testResultName: String,
    )
}
