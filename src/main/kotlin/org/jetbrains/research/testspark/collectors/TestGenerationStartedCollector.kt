package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.research.testspark.core.test.data.CodeType
import org.jetbrains.research.testspark.tools.GenerationTool

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
    private val generationTool: EnumEventField<GenerationTool> = CollectorsHelper.getGenerationTool()
    private val level: EnumEventField<CodeType> = CollectorsHelper.getLevel()

    private val event = group.registerEvent(eventId, generationTool, level)

    override fun getGroup() = group

    fun logEvent(generationTool: GenerationTool, level: CodeType) {
        event.log(generationTool, level)
    }
}