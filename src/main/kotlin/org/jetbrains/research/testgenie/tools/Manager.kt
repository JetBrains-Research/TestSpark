package org.jetbrains.research.testgenie.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.research.testgenie.services.TestCaseDisplayService
import org.jetbrains.research.testgenie.tools.evosuite.TEST_GENERATION_RESULT_TOPIC
import org.jetbrains.research.testgenie.tools.toolImpls.EvoSuite
import org.jetbrains.research.testgenie.tools.toolImpls.Llm

class Manager {

    companion object {
        val tools: List<Tool> = listOf(EvoSuite(), Llm())
        fun generateTestsForClass(e: AnActionEvent) {
            for (tool: Tool in tools) tool.generateTestsForClass(e)
            display(e)
        }

        fun generateTestsForMethod(e: AnActionEvent) {
            for (tool: Tool in tools) tool.generateTestsForMethod(e)
            display(e)
        }

        fun generateTestsForLine(e: AnActionEvent) {
            for (tool: Tool in tools) tool.generateTestsForLine(e)
            display(e)
        }

        fun display(e: AnActionEvent) = AppExecutorUtil.getAppScheduledExecutorService().execute(Display(e, tools))
    }
}

private class Display(e: AnActionEvent, t: List<Tool>) : Runnable {
    val event: AnActionEvent = e
    val tools: List<Tool> = t
    override fun run() {
        val sleepDurationMillis: Long = 2000
        while (true) {
            if (event.project!!.service<TestCaseDisplayService>().testGenerationResultList.size != tools.size) {
                Thread.sleep(sleepDurationMillis)
                continue
            }
            // TODO merge testGenerationResult array
            event.project!!.messageBus.syncPublisher(TEST_GENERATION_RESULT_TOPIC).testGenerationResult(
                event.project!!.service<TestCaseDisplayService>().testGenerationResultList[0]!!,
                event.project!!.service<TestCaseDisplayService>().resultName,
                event.project!!.service<TestCaseDisplayService>().fileUrl,
            )
            return
        }
    }
}
