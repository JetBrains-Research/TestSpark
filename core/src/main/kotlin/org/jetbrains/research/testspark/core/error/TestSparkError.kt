package org.jetbrains.research.testspark.core.error

import org.jetbrains.research.testspark.core.data.TestSparkModule

abstract class TestSparkError(
    open val module: TestSparkModule,
    open val cause: Throwable? = null,
)
