package com.github.mitchellolsthoorn.testgenie.evo

import com.intellij.util.messages.Topic
import org.evosuite.utils.CompactReport

val TEST_GENERATION_RESULT_TOPIC: Topic<TestGenerationResultListener> = Topic.create(
    "TEST_GENERATION_RESULT_TOPIC", TestGenerationResultListener::class.java, Topic.BroadcastDirection.TO_PARENT
)

interface TestGenerationResultListener {
    fun testGenerationResult(test: CompactReport)
}