package com.github.mitchellolsthoorn.testgenie.evosuite

import com.intellij.util.messages.Topic
import org.evosuite.utils.CompactReport

val TEST_GENERATION_RESULT_TOPIC: Topic<TestGenerationResultListener> = Topic.create(
    "TEST_GENERATION_RESULT_TOPIC", TestGenerationResultListener::class.java, Topic.BroadcastDirection.TO_PARENT
)

/**
 * Topic interface for sending and receiving test results produced by evosuite
 *
 * Subscribers to this topic will receive a CompactReport whenever the plugin triggers a test
 * generation job with testgenie.evosuite.Runner
 */
interface TestGenerationResultListener {
    fun testGenerationResult(testReport: CompactReport)
}
