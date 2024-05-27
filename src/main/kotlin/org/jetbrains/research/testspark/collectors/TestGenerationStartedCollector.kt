package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.collectors.data.Technique
import org.jetbrains.research.testspark.data.CodeType

/**
 * This class represents a collector for tracking the event of test generation being started.
 *
 * @property technique The EnumEventField<Technique> instance representing the technique used for test generation.
 * @property level The EnumEventField<CodeType> instance representing the code type for which test generation is performed.
 */
class TestGenerationStartedCollector : CounterUsagesCollector() {
    private val groupId = CollectorsHelper.getTestsSetGroupID()
    private val group = CollectorsHelper.getGroup(groupId)

    private val eventId = "test.generation.started"
    private val technique: EnumEventField<Technique> = CollectorsHelper.getTechnique()
    private val level: EnumEventField<CodeType> = CollectorsHelper.getLevel()

    private val event = group.registerEvent(eventId, technique, level)

    override fun getGroup() = group

    fun logEvent(technique: Technique, level: CodeType) {
        event.log(technique, level)
    }
}
