package org.jetbrains.research.testspark.core.error

import org.jetbrains.research.testspark.core.data.TestSparkModule

sealed class EvoSuiteError(
    cause: Throwable? = null,
) : TestSparkError(
    module = TestSparkModule.EvoSuite,
    cause = cause,
)