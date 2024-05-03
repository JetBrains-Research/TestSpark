package org.jetbrains.research.testspark.core.monitor

interface ErrorMonitor {
    var isErrorOccurred: Boolean

    fun errorOccurred(): Boolean
    fun clear(){
        isErrorOccurred = false
    }
}

class DefaultErrorMonitor: ErrorMonitor {
    override var isErrorOccurred = false
    override fun errorOccurred(): Boolean {
        if (isErrorOccurred) return false
        isErrorOccurred = true
        return true
    }

}