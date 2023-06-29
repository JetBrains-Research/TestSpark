package org.jetbrains.research.testgenie.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.research.testgenie.data.Report
import org.jetbrains.research.testgenie.services.TestCaseDisplayService
import org.jetbrains.research.testgenie.tools.evosuite.TEST_GENERATION_RESULT_TOPIC
import org.jetbrains.research.testgenie.tools.toolImpls.EvoSuite
import org.jetbrains.research.testgenie.tools.toolImpls.Llm

class Manager {

    companion object {
        val tools: List<Tool> = listOf(EvoSuite(), Llm())
        fun generateTestsForClass(e: AnActionEvent) {
            for (tool: Tool in tools) tool.generateTestsForClass(e)
            display(e, tools.size)
        }

        fun generateTestsForMethod(e: AnActionEvent) {
            for (tool: Tool in tools) tool.generateTestsForMethod(e)
            display(e, tools.size)
        }

        fun generateTestsForLine(e: AnActionEvent) {
            for (tool: Tool in tools) tool.generateTestsForLine(e)
            display(e, tools.size)
        }

        fun generateTestsForClassByEvoSuite(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForClass(e)
                    display(e, 1)
                }
            }
        }

        fun generateTestsForClassByLlm(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForClass(e)
                    display(e, 1)
                }
            }
        }

        fun generateTestsForMethodByEvoSuite(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForMethod(e)
                    display(e, 1)
                }
            }
        }

        fun generateTestsForMethodByLlm(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForMethod(e)
                    display(e, 1)
                }
            }
        }

        fun generateTestsForLineByEvoSuite(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForLine(e)
                    display(e, 1)
                }
            }
        }

        fun generateTestsForLineByLlm(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForLine(e)
                    display(e, 1)
                }
            }
        }

        fun display(e: AnActionEvent, numberOfUsedTool: Int) =
            AppExecutorUtil.getAppScheduledExecutorService().execute(Display(e, numberOfUsedTool))
    }
}

private class Display(private val event: AnActionEvent, private val numberOfUsedTool: Int) : Runnable {
    override fun run() {
        val sleepDurationMillis: Long = 2000
        while (true) {
            if (event.project!!.service<TestCaseDisplayService>().testGenerationResultList.size != numberOfUsedTool) {
                Thread.sleep(sleepDurationMillis)
                continue
            }
            event.project!!.messageBus.syncPublisher(TEST_GENERATION_RESULT_TOPIC).testGenerationResult(
                getMergeResult(numberOfUsedTool),
                event.project!!.service<TestCaseDisplayService>().resultName,
                event.project!!.service<TestCaseDisplayService>().fileUrl,
            )
            return
        }
    }

    private fun getMergeResult(numberOfUsedTool: Int): Report {
        if (numberOfUsedTool == 1) {
            return event.project!!.service<TestCaseDisplayService>().testGenerationResultList[0]!!
        }
        TODO("implement merge")
    }
}
