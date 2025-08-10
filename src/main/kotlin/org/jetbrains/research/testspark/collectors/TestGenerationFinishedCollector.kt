package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.LongEventField
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.core.test.data.CodeType
import org.jetbrains.research.testspark.tools.GenerationTool

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
    private val generationTool: EnumEventField<GenerationTool> = CollectorsHelper.getGenerationTool()
    private val level: EnumEventField<CodeType> = CollectorsHelper.getLevel()

    private val event = group.registerEvent(eventId, durationMs, generationTool, level)

    override fun getGroup() = group

    fun logEvent(durationMs: Long, generationTool: GenerationTool, level: CodeType) {
        event.log(durationMs, generationTool, level)
    }
}
