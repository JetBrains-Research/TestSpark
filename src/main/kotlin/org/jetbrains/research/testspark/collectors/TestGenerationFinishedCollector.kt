package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.LongEventField
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.data.Level
import org.jetbrains.research.testspark.data.Technique

class TestGenerationFinishedCollector : CounterUsagesCollector() {
    private val groupId = "tests.set"
    private val group = EventLogGroup(groupId, 1)

    private val eventId = "test.generation.finished"
    private val durationMs: LongEventField = LongEventField("durationMs")
    private val technique: EnumEventField<Technique> = EventFields.Enum("technique", Technique::class.java)
    private val level: EnumEventField<Level> = EventFields.Enum("level", Level::class.java)

    private val event = group.registerEvent(eventId, durationMs, technique, level)

    override fun getGroup() = group

    fun logEvent(durationMs: Long, technique: Technique, level: Level) {
        event.log(durationMs, technique, level)
    }
}
