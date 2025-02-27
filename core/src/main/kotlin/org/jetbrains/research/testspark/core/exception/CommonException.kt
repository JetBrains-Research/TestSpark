package org.jetbrains.research.testspark.core.exception

import org.jetbrains.research.testspark.core.data.TestSparkModule

sealed class CommonException(module: TestSparkModule, cause: Throwable? = null) : TestSparkException(
    module = module,
    cause = cause,
)

class ProcessCancelledException(module: TestSparkModule) : CommonException(module)
