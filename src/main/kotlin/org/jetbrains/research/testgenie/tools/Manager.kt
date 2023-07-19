package org.jetbrains.research.testgenie.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.research.testgenie.data.Report
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.services.ErrorService
import org.jetbrains.research.testgenie.services.RunnerService
import org.jetbrains.research.testgenie.tools.evosuite.EvoSuite
import org.jetbrains.research.testgenie.tools.llm.Llm
import org.jetbrains.research.testgenie.tools.template.Tool

class Manager {
    companion object {
        val tools: List<Tool> = listOf(EvoSuite(), Llm())
        fun generateTestsForClass(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (tool: Tool in tools) tool.generateTestsForClass(e)
            display(e, tools.size)
        }

        fun generateTestsForMethod(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (tool: Tool in tools) tool.generateTestsForMethod(e)
            display(e, tools.size)
        }

        fun generateTestsForLine(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (tool: Tool in tools) tool.generateTestsForLine(e)
            display(e, tools.size)
        }

        fun generateTestsForClassByEvoSuite(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForClass(e)
                    display(e, 1)
                }
            }
        }

        fun generateTestsForClassByLlm(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForClass(e)
                    display(e, 1)
                }
            }
        }

        fun generateTestsForMethodByEvoSuite(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForMethod(e)
                    display(e, 1)
                }
            }
        }

        fun generateTestsForMethodByLlm(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForMethod(e)
                    display(e, 1)
                }
            }
        }

        fun generateTestsForLineByEvoSuite(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForLine(e)
                    display(e, 1)
                }
            }
        }

        fun generateTestsForLineByLlm(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForLine(e)
                    display(e, 1)
                }
            }
        }

        private fun display(e: AnActionEvent, numberOfUsedTool: Int) =
            AppExecutorUtil.getAppScheduledExecutorService().execute(Display(e, numberOfUsedTool))
    }
}

private class Display(private val event: AnActionEvent, private val numberOfUsedTool: Int) : Runnable {
    override fun run() {
        val sleepDurationMillis: Long = 1000
        while (true) {
            if (event.project!!.service<Workspace>().testGenerationData.testGenerationResultList.size != numberOfUsedTool) {
                // there is some error during the process running
                if (event.project!!.service<ErrorService>().isErrorOccurred()) break
                Thread.sleep(sleepDurationMillis)
                continue
            }

            event.project!!.messageBus.syncPublisher(TEST_GENERATION_RESULT_TOPIC).testGenerationResult(
                getMergeResult(numberOfUsedTool),
                event.project!!.service<Workspace>().testGenerationData.resultName,
                event.project!!.service<Workspace>().testGenerationData.fileUrl,
            )

            break
        }

        event.project!!.service<RunnerService>().clear()
    }

    private fun getMergeResult(numberOfUsedTool: Int): Report {
        if (numberOfUsedTool == 1) {
            return event.project!!.service<Workspace>().testGenerationData.testGenerationResultList[0]!!
        }
        TODO("implement merge")
    }
}
