package org.jetbrains.research.testgenie.services

class ErrorService {
    private var isErrorOccurred: Boolean = false
    fun isErrorOccurred() = isErrorOccurred

    fun errorOccurred() {
        isErrorOccurred = true
    }

    fun clear() {
        isErrorOccurred = false
    }
}
