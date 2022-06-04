package nl.tudelft.ewi.se.ciselab.testgenie.evosuite.validation

import com.intellij.util.messages.Topic

val VALIDATION_RESULT_TOPIC: Topic<ValidationResultListener> = Topic.create(
    "VALIDATION_RESULT_TOPIC", ValidationResultListener::class.java, Topic.BroadcastDirection.TO_PARENT
)

/**
 * Topic interface for sending and receiving test results produced by evosuite
 *
 * Subscribers to this topic will receive a CompactReport whenever the plugin triggers a test
 * generation job with testgenie.evosuite.Runner
 */
interface ValidationResultListener {
    fun validationResult(junitResult: Validator.JUnitResult)
}
