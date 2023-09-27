package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service

/**
 * Service class for handling error occurrences.
 */
@Service(Service.Level.PROJECT)
class ErrorService {
    private var isErrorOccurred: Boolean = false
    fun isErrorOccurred() = isErrorOccurred

    /**
     * Checks if an error has occurred.
     *
     * @return true if an error has occurred, false otherwise.
     */
    fun errorOccurred(): Boolean {
        if (isErrorOccurred()) return false
        isErrorOccurred = true
        return true
    }

    /**
     * Clears the error state.
     */
    fun clear() {
        isErrorOccurred = false
    }
}
