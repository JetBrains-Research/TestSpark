package org.jetbrains.research.testspark.core.exception

import org.jetbrains.research.testspark.core.data.TestSparkModule

/**
 * Represents custom exceptions within TestSpark.
 *
 * This class serves as a base class for specific exceptions.
 *
 * @param message A descriptive message explaining the error that led to the exception.
 */
abstract class TestSparkException(
    open val module: TestSparkModule,
    override val cause: Throwable? = null,
) : RuntimeException(cause)
