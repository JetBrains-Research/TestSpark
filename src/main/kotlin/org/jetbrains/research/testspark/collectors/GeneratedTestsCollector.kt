package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.IntEventField
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.Technique

/**
 * This class represents a collector for tracking the event of generated tests.
 *
 * @property count the IntEventField instance representing the count for the event
 * @property technique the EnumEventField<Technique> instance representing the technique for the event
 * @property level the EnumEventField<CodeType> instance representing the code type for the event
 * @property event the VarargEvent instance representing the generated tests event
 */
class GeneratedTestsCollector : CounterUsagesCollector() {
    private val groupId = CollectorsHelper.getTestsSetGroupID()
    private val group = CollectorsHelper.getGroup(groupId)

    private val eventId = "generated.tests"
    private val count: IntEventField = CollectorsHelper.getCount()
    private val technique: EnumEventField<Technique> = CollectorsHelper.getTechnique()
    private val level: EnumEventField<CodeType> = CollectorsHelper.getLevel()

    private val event = group.registerEvent(eventId, count, technique, level)

    override fun getGroup() = group

    fun logEvent(count: Int, technique: Technique, level: CodeType) {
        event.log(count, technique, level)
    }
}
