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

        fun display(e: AnActionEvent) = AppExecutorUtil.getAppScheduledExecutorService().execute(Display(e))
    }
}

private class Display(e: AnActionEvent) : Runnable {
    val event: AnActionEvent = e
    override fun run() {
        val sleepDurationMillis: Long = 2000
        while (true) {
            // TODO fix if
            if (event.project!!.service<TestCaseDisplayService>().testGenerationResult == null) {
                Thread.sleep(sleepDurationMillis)
                continue
            }
            event.project!!.messageBus.syncPublisher(TEST_GENERATION_RESULT_TOPIC).testGenerationResult(
                event.project!!.service<TestCaseDisplayService>().testGenerationResult!!,
                event.project!!.service<TestCaseDisplayService>().resultName,
                event.project!!.service<TestCaseDisplayService>().fileUrl,
            )
            return
        }
    }
}
