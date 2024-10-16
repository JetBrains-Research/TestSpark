package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager
import org.jetbrains.research.testspark.tools.llm.LlmSettingsArguments

abstract class IJRequestManager(protected val project: Project) : RequestManager(token = LlmSettingsArguments(project).getToken())
