package org.jetbrains.research.testgenie.evosuite.validation

import com.intellij.util.messages.Topic

val VALIDATION_RESULT_TOPIC: Topic<ValidationResultListener> = Topic.create(
    "VALIDATION_RESULT_TOPIC", ValidationResultListener::class.java, Topic.BroadcastDirection.TO_PARENT
)

/**
 * Topic interface for sending and receiving results of test validation
 *
 * Subscribers to this topic will receive a validation result whenever the user triggers test
 * generation validation
 */
interface ValidationResultListener {
    fun validationResult(junitResult: Validator.JUnitResult)
}
