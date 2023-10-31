package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.data.Level
import org.jetbrains.research.testspark.data.Technique

class LikedDislikedCollector : CounterUsagesCollector() {
    private val groupId = "individual.tests"
    private val group = EventLogGroup(groupId, 1)

    private val eventId = "liked.disliked"
    private val liked = EventFields.Boolean("liked")
    private val testId = EventFields.StringValidatedByRegexp("id", CollectorsHelper().testIDRegex.pattern)
    private val technique: EnumEventField<Technique> = EventFields.Enum("technique", Technique::class.java)
    private val level: EnumEventField<Level> = EventFields.Enum("level", Level::class.java)
    private val isModified = EventFields.Boolean("is_modified")

    private val event = group.registerVarargEvent(
        eventId,
        liked,
        testId,
        technique,
        level,
        isModified,
    )

    override fun getGroup() = group

    fun logEvent(liked: Boolean, testId: String, technique: Technique, level: Level, isModified: Boolean) {
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
