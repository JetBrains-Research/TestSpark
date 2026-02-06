package org.jetbrains.research.testspark.core.error

import org.jetbrains.research.testspark.core.data.TestSparkModule

data class HttpError(
    val httpCode: Int? = null,
    val message: String? = null,
    override val module: TestSparkModule = TestSparkModule.Llm(),
    override val cause: Throwable? = null,
) : TestSparkError(module, cause)
