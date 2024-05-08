package org.jetbrains.research.testspark.data

import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager

data class UIContext(
    val projectContext: ProjectContext,
    val testGenerationOutput: TestGenerationData,
    val testCasesUIContext: TestCasesUIContext,
    var requestManager: RequestManager? = null,
)
