package org.jetbrains.research.testspark.data

import org.jetbrains.research.testspark.actions.controllers.IndicatorController
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.generation.llm.ChatSessionManager
import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor

data class UIContext(
    val projectContext: ProjectContext,
    val testGenerationOutput: TestGenerationData,
    var chatSessionManager: ChatSessionManager,
    val indicatorController: IndicatorController,
    val errorMonitor: ErrorMonitor,
)
