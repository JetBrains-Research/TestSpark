package org.jetbrains.research.testgenie.services

class ErrorService {
    private var isErrorOccurred: Boolean = false
    fun isErrorOccurred() = isErrorOccurred

    fun errorOccurred(): Boolean {
        if (isErrorOccurred()) return false
        isErrorOccurred = true
        return true
    }

    fun clear() {
        isErrorOccurred = false
    }
}
