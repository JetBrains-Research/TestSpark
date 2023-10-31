package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.intellij.internal.statistic.eventLog.events.IntEventField
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.data.Level
import org.jetbrains.research.testspark.data.Technique

class GeneratedTestsCollector : CounterUsagesCollector() {
    private val groupId = "tests.set"
    private val group = EventLogGroup(groupId, 1)

    private val eventId = "generated.tests"
    private val count: IntEventField = IntEventField("count")
    private val technique: EnumEventField<Technique> = EventFields.Enum("technique", Technique::class.java)
    private val level: EnumEventField<Level> = EventFields.Enum("level", Level::class.java)

    private val event = group.registerVarargEvent(eventId, count, technique, level)

    override fun getGroup() = group

    fun logEvent(count: Int, technique: Technique, level: Level) {
        val data: List<EventPair<*>> = arrayListOf(
            this.count.with(count),
            this.technique.with(technique),
            this.level.with(level),
        )
        event.log(data)
    }
}
