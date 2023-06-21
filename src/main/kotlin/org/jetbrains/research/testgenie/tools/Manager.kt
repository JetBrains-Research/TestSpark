package org.jetbrains.research.testgenie.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.util.concurrency.AppExecutorUtil
import org.evosuite.utils.CompactReport
import org.jetbrains.research.testgenie.services.TestCaseDisplayService
import org.jetbrains.research.testgenie.tools.evosuite.TEST_GENERATION_RESULT_TOPIC
import org.jetbrains.research.testgenie.tools.toolImpls.EvoSuite
import org.jetbrains.research.testgenie.tools.toolImpls.Llm

class Manager {

    companion object {
        val tools: List<Tool> = listOf(EvoSuite(), Llm())
        fun generateTestsForClass(e: AnActionEvent) {
            for (tool: Tool in tools) tool.generateTestsForClass(e)
            display(e, (tools.indices).toList())
        }

        fun generateTestsForMethod(e: AnActionEvent) {
            for (tool: Tool in tools) tool.generateTestsForMethod(e)
            display(e, (tools.indices).toList())
        }

        fun generateTestsForLine(e: AnActionEvent) {
            for (tool: Tool in tools) tool.generateTestsForLine(e)
            display(e, (tools.indices).toList())
        }

        fun generateTestsForClassByEvoSuite(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForClass(e)
                    display(e, listOf(index))
                }
            }
        }

        fun generateTestsForClassByLlm(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForClass(e)
                    display(e, listOf(index))
                }
            }
        }

        fun generateTestsForMethodByEvoSuite(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForMethod(e)
                    display(e, listOf(index))
                }
            }
        }

        fun generateTestsForMethodByLlm(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForMethod(e)
                    display(e, listOf(index))
                }
            }
        }

        fun generateTestsForLineByEvoSuite(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForLine(e)
                    display(e, listOf(index))
                }
            }
        }

        fun generateTestsForLineByLlm(e: AnActionEvent) {
            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForLine(e)
                    display(e, listOf(index))
                }
            }
        }

        fun display(e: AnActionEvent, indexes: List<Int>) = AppExecutorUtil.getAppScheduledExecutorService().execute(Display(e, indexes))
    }
}

private class Display(e: AnActionEvent, i: List<Int>) : Runnable {
    val event: AnActionEvent = e
    val indexes: List<Int> = i
    override fun run() {
        val sleepDurationMillis: Long = 2000
        while (true) {
            if (event.project!!.service<TestCaseDisplayService>().testGenerationResultList.size != indexes.size) {
                Thread.sleep(sleepDurationMillis)
                continue
            }
            event.project!!.messageBus.syncPublisher(TEST_GENERATION_RESULT_TOPIC).testGenerationResult(
                getMergeResult(indexes),
                event.project!!.service<TestCaseDisplayService>().resultName,
                event.project!!.service<TestCaseDisplayService>().fileUrl,
            )
            return
        }
    }

    private fun getMergeResult(indexes: List<Int>): CompactReport {
        if (indexes.size == 1) {
            return event.project!!.service<TestCaseDisplayService>().testGenerationResultList[indexes[0]]!!
        }
        TODO("implement merge")
    }
}
