package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.Technique

/**
 * This class represents a collector for tracking the event of test generation being started.
 *
 * @property technique The EnumEventField<Technique> instance representing the technique used for test generation.
 * @property level The EnumEventField<CodeType> instance representing the code type for which test generation is performed.
 */
class TestGenerationStartedCollector : CounterUsagesCollector() {
    private val groupId = "tests.set"
    private val group = EventLogGroup(groupId, 1)

    private val eventId = "test.generation.started"
    private val technique: EnumEventField<Technique> = EventFields.Enum("technique", Technique::class.java)
    private val level: EnumEventField<CodeType> = EventFields.Enum("level", CodeType::class.java)

    private val event = group.registerEvent(eventId, technique, level)

    override fun getGroup() = group

    fun logEvent(technique: Technique, level: CodeType) {
        event.log(technique, level)
    }
}
