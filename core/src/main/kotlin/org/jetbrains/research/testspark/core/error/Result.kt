package org.jetbrains.research.testspark.core.error

sealed interface Result<out D, out E: TestSparkError> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Failure<out E: TestSparkError>(val error: E): Result<Nothing, E>

    fun getDataOrNull(): D? = if (this is Success) data else null

    fun <R> mapData(transform: (D) -> R): Result<R, E> {
        return when (this) {
            is Success -> Success(transform(data))
            is Failure -> Failure(error)
        }
    }

    fun <R : TestSparkError> mapError(transform: (E) -> R): Result<D, R> {
        return when (this) {
            is Success -> Success(data)
            is Failure -> Failure(transform(error))
        }
    }
}
