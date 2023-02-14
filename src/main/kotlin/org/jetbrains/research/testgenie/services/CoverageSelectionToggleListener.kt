package org.jetbrains.research.testgenie.services

import com.intellij.openapi.editor.Editor
import com.intellij.util.messages.Topic

val COVERAGE_SELECTION_TOGGLE_TOPIC: Topic<CoverageSelectionToggleListener> = Topic.create(
    "COVERAGE_SELECTION_TOGGLE_TOPIC", CoverageSelectionToggleListener::class.java, Topic.BroadcastDirection.TO_PARENT
)

/**
 * Topic interface for sending and receiving test results produced by evosuite
 *
 * Subscribers to this topic will receive a CompactReport whenever the plugin triggers a test
 * generation job with testgenie.evosuite.Runner
 */
interface CoverageSelectionToggleListener {
    fun testGenerationResult(testName: String, selected: Boolean, editor: Editor)
}
