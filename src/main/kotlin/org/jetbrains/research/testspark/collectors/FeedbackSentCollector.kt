package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.Technique

/**
 * This class represents a collector for tracking the event of llm feedback in test cases being sent.
 *
 * @property testId the EventFields.StringValidatedByRegexp instance representing the test ID for the event
 * @property technique the EnumEventField<Technique> instance representing the technique for the event
 * @property level the EnumEventField<CodeType> instance representing the code type for the event
 * @property isModified the EventFields.Boolean instance representing whether the test was modified for the event
 */
class FeedbackSentCollector : CounterUsagesCollector() {
    private val groupId = CollectorsHelper.getIndividualTestsGroupID()
    private val group = CollectorsHelper.getGroup(groupId)

    private val eventId = "feedback.sent"
    private val testId = CollectorsHelper.getTestID()
    private val technique: EnumEventField<Technique> = CollectorsHelper.getTechnique()
    private val level: EnumEventField<CodeType> = CollectorsHelper.getLevel()
    private val isModified = EventFields.Boolean("is_modified")

    private val event = group.registerVarargEvent(eventId, testId, technique, level, isModified)

    override fun getGroup() = group

    fun logEvent(testId: String, technique: Technique, level: CodeType, isModified: Boolean) {
        val data: List<EventPair<*>> = arrayListOf(
            this.testId.with(testId),
            this.technique.with(technique),
            this.level.with(level),
            this.isModified.with(isModified),
        )
        event.log(data)
    }
}
