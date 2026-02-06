package org.jetbrains.research.testspark.uiTest.utils

import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker

// The code here was copied from JetBrains/intellij-ui-test-robot library to create UI testing for the plugin.
// Link: https://github.com/JetBrains/intellij-ui-test-robot/blob/master/ui-test-example/src/test/kotlin/org/intellij/examples/simple/plugin/utils/StepsLogger.kt
object StepsLogger {
    private var initializaed = false

    @JvmStatic
    private val lock = Any()
    fun init() = synchronized(lock) {
        if (initializaed.not()) {
            StepWorker.registerProcessor(StepLogger())
            initializaed = true
        }
    }
}
