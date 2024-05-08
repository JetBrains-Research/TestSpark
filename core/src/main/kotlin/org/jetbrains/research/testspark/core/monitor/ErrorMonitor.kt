package org.jetbrains.research.testspark.core.monitor

interface ErrorMonitor {
    var errorOccurred: Boolean


    fun notifyErrorOccurrence(): Boolean
    fun clear() {
        errorOccurred = false
    }
}

class DefaultErrorMonitor : ErrorMonitor {
    override var errorOccurred = false
    override fun notifyErrorOccurrence(): Boolean {
        if (errorOccurred) return false
        errorOccurred = true
        return true
    }
}
