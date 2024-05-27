package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.LongEventField
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.Technique

/**
 * This class represents a collector for tracking the event of test generation being finished.
 *
 * @property durationMs The LongEventField instance representing the duration of test generation.
 * @property technique The EnumEventField<Technique> instance representing the technique used for test generation.
 * @property level The EnumEventField<CodeType> instance representing the code type for which test generation was performed.
 */
class TestGenerationFinishedCollector : CounterUsagesCollector() {
    private val groupId = CollectorsHelper.getTestsSetGroupID()
    private val group = CollectorsHelper.getGroup(groupId)

    private val eventId = "test.generation.finished"
    private val durationMs: LongEventField = LongEventField("durationMs")
    private val technique: EnumEventField<Technique> = CollectorsHelper.getTechnique()
    private val level: EnumEventField<CodeType> = CollectorsHelper.getLevel()

    private val event = group.registerEvent(eventId, durationMs, technique, level)

    override fun getGroup() = group

    fun logEvent(durationMs: Long, technique: Technique, level: CodeType) {
        event.log(durationMs, technique, level)
    }
}
