package org.jetbrains.research.testspark.core.error

import org.jetbrains.research.testspark.core.data.TestSparkModule

sealed class KexError(
    cause: Throwable? = null,
) : TestSparkError(
    module = TestSparkModule.Kex,
    cause = cause,
)
