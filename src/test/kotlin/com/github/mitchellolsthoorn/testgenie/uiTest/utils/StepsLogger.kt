package com.github.mitchellolsthoorn.testgenie.uiTest.utils

import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker
// The code here was copied from JetBrains/intellij-ui-test-robot library, in order to experiment with the UI testing.
object StepsLogger {
    private var initializaed = false
    @JvmStatic
    fun init() = synchronized(initializaed) {
        if (initializaed.not()) {
            StepWorker.registerProcessor(StepLogger())
            initializaed = true
        }
    }
}