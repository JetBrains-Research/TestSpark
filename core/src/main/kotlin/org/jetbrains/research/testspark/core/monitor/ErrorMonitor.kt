package org.jetbrains.research.testspark.core.monitor

/**
 * This interface is used for contract adherence in error monitoring.
 */
interface ErrorMonitor {

    /**
     * Notifies when an error has occurred. If this function is called,
     * it means an error has occurred and should return true.
     *
     * @return Boolean
     */
    fun notifyErrorOccurrence(): Boolean

    /**
     * Checks if an error has occurred. If an error has occurred, this
     * function should return true; otherwise, it should return false.
     *
     * @return Boolean
     */
    fun hasErrorOccurred(): Boolean

    /**
     * Clears the status of the error occurrence. Calling this function should
     * reset the error state to a state as if no error had occurred.
     */
    fun clear()
}

/**
 * An abstract ErrorMonitor that provides basic implementation of ErrorMonitor.
 * This class includes default behavior for hasErrorOccurred() and clear() methods.
 * It also consists of 'errorOccurred' variable to keep track of error occurrence.
 */
abstract class AbstractErrorMonitor : ErrorMonitor {
    protected var errorOccurred: Boolean = false

    /**
     * Returns the current state of error occurrence.
     * @return errorOccurred - a Boolean value representing whether an error has occurred or not.
     */
    override fun hasErrorOccurred(): Boolean {
        return errorOccurred
    }

    /**
     * Resets the state of error occurrence by setting 'errorOccurred' to false.
     */
    override fun clear() {
        errorOccurred = false
    }
}

/**
 * A specific implementation of AbstractErrorMonitor that includes behavior for notifyErrorOccurrence() method.
 * If an error has already occurred, it returns false, otherwise it sets 'errorOccurred' to true and returns true.
 */
class DefaultErrorMonitor : AbstractErrorMonitor() {

    /**
     * Handles the case when an error occurrence has been notified.
     * If an error has already occurred, it ignores the notification and returns false.
     * If an error has not occurred yet, it sets 'errorOccurred' to true and return true.
     * @return Boolean value indicating whether the error notification has been handled or not.
     */
    override fun notifyErrorOccurrence(): Boolean {
        if (errorOccurred) return false
        errorOccurred = true
        return true
    }
}
