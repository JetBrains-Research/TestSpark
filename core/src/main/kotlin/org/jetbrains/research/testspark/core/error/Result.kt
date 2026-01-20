package org.jetbrains.research.testspark.core.error

sealed interface Result<out D> {
    data class Success<out D>(
        val data: D,
    ) : Result<D>

    data class Failure(
        val error: TestSparkError,
    ) : Result<Nothing>

    fun getDataOrNull(): D? = if (this is Success) data else null

    fun isSuccess(): Boolean = this is Success

    fun isFailure(): Boolean = this is Failure

    fun <R> mapData(transform: (D) -> R): Result<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Failure -> Failure(error)
        }

    fun <R : TestSparkError> mapError(transform: (TestSparkError) -> R): Result<D> =
        when (this) {
            is Success -> Success(data)
            is Failure -> Failure(transform(error))
        }
}
