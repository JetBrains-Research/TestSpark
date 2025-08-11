package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.intellij.internal.statistic.eventLog.events.IntEventField
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.core.test.data.CodeType
import org.jetbrains.research.testspark.tools.GenerationTool

/**
 * This class represents a collector for tracking integrated tests to the project events.
 *
 * @property count the IntEventField instance representing the count for the event
 * @property technique the EnumEventField<Technique> instance representing the technique for the event
 * @property level the EnumEventField<CodeType> instance representing the code type for the event
 * @property modifiedTestsCount the IntEventField instance representing the modified tests count for the event
 * */
class IntegratedTestsCollector : CounterUsagesCollector() {
    private val groupId = CollectorsHelper.getTestsSetGroupID()
    private val group = CollectorsHelper.getGroup(groupId)

    private val eventId = "integrated.tests"
    private val count: IntEventField = CollectorsHelper.getCount()
    private val generationTool: EnumEventField<GenerationTool> = CollectorsHelper.getGenerationTool()
    private val level: EnumEventField<CodeType> = CollectorsHelper.getLevel()
    private val modifiedTestsCount: IntEventField = IntEventField("modifiedTestsCount")

    private val event = group.registerVarargEvent(eventId, count, generationTool, level, modifiedTestsCount)

    override fun getGroup() = group

    fun logEvent(
        count: Int,
        generationTool: GenerationTool,
        level: CodeType,
        modifiedTestsCount: Int,
    ) {
        val data: List<EventPair<*>> =
            arrayListOf(
                this.count.with(count),
                this.generationTool.with(generationTool),
                this.level.with(level),
                this.modifiedTestsCount.with(modifiedTestsCount),
            )
        event.log(data)
    }
}
