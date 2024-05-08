package org.jetbrains.research.testspark.core.monitor

interface ErrorMonitor {
    fun notifyErrorOccurrence(): Boolean
    fun isErrorOccurred(): Boolean
    fun clear()
}

abstract class AbstractErrorMonitor : ErrorMonitor {
    protected var errorOccurred: Boolean = false
    override fun isErrorOccurred(): Boolean {
        return errorOccurred
    }
    override fun clear() {
        errorOccurred = false
    }
}

class DefaultErrorMonitor : AbstractErrorMonitor() {
    override fun notifyErrorOccurrence(): Boolean {
        if (errorOccurred) return false
        errorOccurred = true
        return true
    }
}
