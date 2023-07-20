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

/**
 * Provides methods for generating tests using different tools.
 */
class Manager {
    companion object {
        val tools: List<Tool> = listOf(EvoSuite(), Llm())

        /**
         * Generates tests for a given class based on the provided
         * AnActionEvent.
         *
         * @param e the AnActionEvent used to trigger the test generation
         */
        fun generateTestsForClass(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (tool: Tool in tools) tool.generateTestsForClass(e)
            display(e, tools.size)
        }

        /**
         * Generates tests for a given method.
         *
         * @param e The AnActionEvent containing information about the action event.
         * @throws NullPointerException if e.project is null or if the RunnerService is not available.
         */
        fun generateTestsForMethod(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (tool: Tool in tools) tool.generateTestsForMethod(e)
            display(e, tools.size)
        }

        /**
         * Generates tests for a specific line of code.
         *
         * @param e The AnActionEvent object representing the action event.
         */
        fun generateTestsForLine(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (tool: Tool in tools) tool.generateTestsForLine(e)
            display(e, tools.size)
        }

        /**
         * Generates tests for a class using EvoSuite.
         *
         * @param e The AnActionEvent object representing the action event.
         */
        fun generateTestsForClassByEvoSuite(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForClass(e)
                    display(e, 1)
                }
            }
        }

        /**
         * Generates tests for a class using Llm tool.
         *
         * @param e the AnActionEvent representing the action event.
         */
        fun generateTestsForClassByLlm(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForClass(e)
                    display(e, 1)
                }
            }
        }

        /**
         * Generates tests for a specific method using EvoSuite.
         *
         * @param e The AnActionEvent object representing the user action.
         */
        fun generateTestsForMethodByEvoSuite(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForMethod(e)
                    display(e, 1)
                }
            }
        }

        /**
         * Generates tests for a given method based on Llm tool.
         *
         * @param e The AnActionEvent object containing information about the action event.
         */
        fun generateTestsForMethodByLlm(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForMethod(e)
                    display(e, 1)
                }
            }
        }

        /**
         * Generates tests for a specific line using EvoSuite tool.
         *
         * @param e AnActionEvent representing the action event.
         */
        fun generateTestsForLineByEvoSuite(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == EvoSuite().name) {
                    tools[index].generateTestsForLine(e)
                    display(e, 1)
                }
            }
        }

        /**
         * Generates tests for a line using Llm tool.
         *
         * @param e The AnActionEvent containing the information about the action
         */
        fun generateTestsForLineByLlm(e: AnActionEvent) {
            if (e.project!!.service<RunnerService>().isGeneratorRunning()) return

            for (index in tools.indices) {
                if (tools[index].name == Llm().name) {
                    tools[index].generateTestsForLine(e)
                    display(e, 1)
                }
            }
        }

        /**
         * Displays the given AnActionEvent and the number of used tools.
         *
         * @param e The AnActionEvent to be displayed.
         * @param numberOfUsedTool The number of used tools to be displayed.
         */
        private fun display(e: AnActionEvent, numberOfUsedTool: Int) =
            AppExecutorUtil.getAppScheduledExecutorService().execute(Display(e, numberOfUsedTool))
    }
}

/**
 * A private class that displays the test generation result to the user.
 *
 * @param event The action event that triggered the display.
 * @param numberOfUsedTool The number of test generation tools used.
 */
private class Display(private val event: AnActionEvent, private val numberOfUsedTool: Int) : Runnable {
    override fun run() {
        // waiting time after each iteration
        val sleepDurationMillis: Long = 1000

        // waiting for the generation result
        while (true) {
            // checks if all generator are finished their work
            if (event.project!!.service<Workspace>().testGenerationData.testGenerationResultList.size != numberOfUsedTool) {
                // there is some error during the process running
                if (event.project!!.service<ErrorService>().isErrorOccurred()) break
                Thread.sleep(sleepDurationMillis)
                continue
            }

            // sends result to Workspace
            event.project!!.messageBus.syncPublisher(TEST_GENERATION_RESULT_TOPIC).testGenerationResult(
                getMergeResult(numberOfUsedTool),
                event.project!!.service<Workspace>().testGenerationData.resultName,
                event.project!!.service<Workspace>().testGenerationData.fileUrl,
            )

            break
        }

        event.project!!.service<RunnerService>().clear()
    }

    /**
     * Retrieves the merged result of a test generation process.
     *
     * @param numberOfUsedTool The number of tools used for the test generation.
     * @return The merged report containing the results of the test generation process.
     */
    private fun getMergeResult(numberOfUsedTool: Int): Report {
        if (numberOfUsedTool == 1) {
            return event.project!!.service<Workspace>().testGenerationData.testGenerationResultList[0]!!
        }
        TODO("implement merge")
    }
}
