package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.collectors.data.Technique
import org.jetbrains.research.testspark.data.CodeType

/**
 * This class represents a collector for tracking the event of test cases being liked or unliked.
 *
 * @property liked the EventFields.Boolean instance representing the liked/unliked status for the event
 * @property testId the EventFields.StringValidatedByRegexp instance representing the test ID for the event
 * @property technique the EnumEventField<Technique> instance representing the technique for the event
 * @property level the EnumEventField<CodeType> instance representing the code type for the event
 * @property isModified the EventFields.Boolean instance representing whether the test was modified for the event
 */
class LikedDislikedCollector : CounterUsagesCollector() {
    private val groupId = CollectorsHelper.getIndividualTestsGroupID()
    private val group = CollectorsHelper.getGroup(groupId)

    private val eventId = "liked.disliked"
    private val liked = EventFields.Boolean("liked")
    private val testId = CollectorsHelper.getTestID()
    private val technique: EnumEventField<Technique> = CollectorsHelper.getTechnique()
    private val level: EnumEventField<CodeType> = CollectorsHelper.getLevel()
    private val isModified = EventFields.Boolean("is_modified")

    private val event = group.registerVarargEvent(eventId, liked, testId, technique, level, isModified)

    override fun getGroup() = group

    fun logEvent(liked: Boolean, testId: String, technique: Technique, level: CodeType, isModified: Boolean) {
        val data: List<EventPair<*>> = arrayListOf(
            this.liked.with(liked),
            this.testId.with(testId),
            this.technique.with(technique),
            this.level.with(level),
            this.isModified.with(isModified),
        )
        event.log(data)
    }
}
