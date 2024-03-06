package org.jetbrains.research.testspark.tools

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.services.ReportLockingService
import org.jetbrains.research.testspark.services.RunnerService
import org.jetbrains.research.testspark.services.TestGenerationDataService
import org.jetbrains.research.testspark.tools.evosuite.EvoSuite
import org.jetbrains.research.testspark.tools.llm.Llm
import org.jetbrains.research.testspark.tools.template.Tool

/**
 * Provides methods for generating tests using different tools.
 */
class Manager {
    companion object {
        val tools: List<Tool> = listOf(EvoSuite(), Llm())

        /**
         * Generates tests for a given class using EvoSuite.
         *
         * @param project The project in which the class is located.
         * @param psiFile The PsiFile containing the class.
         * @param caret The caret object representing the current position in the file.
         * @param fileUrl The URL of the class file.
         * @param testSamplesCode The code for the sample tests.
         *
         * @see RunnerService.isGeneratorRunning
         * @see EvoSuite.generateTestsForClass
         * @see RunnerService.display
         */
        fun generateTestsForClassByEvoSuite(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String) {
            if (project.service<RunnerService>().isGeneratorRunning()) return

            EvoSuite().generateTestsForClass(project, psiFile, caret, fileUrl, testSamplesCode)
            display(project, 1)
        }

        /**
         * Generates tests for a class using Llm.
         *
         * @param project The project in which the class is located.
         * @param psiFile The PsiFile representing the class.
         * @param caret The Caret object representing the current cursor position.
         * @param fileUrl The URL of the file containing the class.
         * @param testSamplesCode The code to be used as test samples.
         */
        fun generateTestsForClassByLlm(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String) {
            if (project.service<RunnerService>().isGeneratorRunning()) return

            Llm().generateTestsForClass(project, psiFile, caret, fileUrl, testSamplesCode)
            display(project, 1)
        }

        /**
         * Generates tests for a given method using EvoSuite.
         *
         * @param project   The current project.
         * @param psiFile   The PSI file containing the method.
         * @param caret     The caret position in the editor.
         * @param fileUrl   The URL of the file being tested.
         * @param testSamplesCode   The sample code for testing.
         */
        fun generateTestsForMethodByEvoSuite(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String) {
            if (project.service<RunnerService>().isGeneratorRunning()) return

            EvoSuite().generateTestsForMethod(project, psiFile, caret, fileUrl, testSamplesCode)
            display(project, 1)
        }

        /**
         * Generates tests for a method using the Llm technique.
         *
         * @param project The project where the method is located.
         * @param psiFile The PSI file where the method is located.
         * @param caret The caret position in the PSI file.
         * @param fileUrl The URL of the file where the method is located.
         * @param testSamplesCode The code samples used for generating tests.
         */
        fun generateTestsForMethodByLlm(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String) {
            if (project.service<RunnerService>().isGeneratorRunning()) return

            Llm().generateTestsForMethod(project, psiFile, caret, fileUrl, testSamplesCode)
            display(project, 1)
        }

        /**
         * Generates tests for a specific line in a file using EvoSuite.
         *
         * @param project The current project.
         * @param psiFile The PSI file in which the line exists.
         * @param caret The caret position.
         * @param fileUrl The URL of the file.
         * @param testSamplesCode The code representing the test samples.
         */
        fun generateTestsForLineByEvoSuite(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String) {
            if (project.service<RunnerService>().isGeneratorRunning()) return

            EvoSuite().generateTestsForLine(project, psiFile, caret, fileUrl, testSamplesCode)
            display(project, 1)
        }

        /**
         * Generates tests for a line of code using the Language Level Migration Assistant (LLM).
         *
         * @param project The current IntelliJ project.
         * @param psiFile The PsiFile object representing the code file.
         * @param caret The Caret object representing the cursor position.
         * @param fileUrl The URL of the file to generate tests for. Can be null.
         * @param testSamplesCode The code for the test samples.
         */
        fun generateTestsForLineByLlm(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String) {
            if (project.service<RunnerService>().isGeneratorRunning()) return

            Llm().generateTestsForLine(project, psiFile, caret, fileUrl, testSamplesCode)
            display(project, 1)
        }

        /**
         * Displays the given project with the specified number of used tools.
         * This method is executed asynchronously.
         *
         * @param project The project to be displayed.
         * @param numberOfUsedTool The number of used tools in the project.
         */
        private fun display(project: Project, numberOfUsedTool: Int) =
            AppExecutorUtil.getAppScheduledExecutorService().execute(Display(project, numberOfUsedTool))
    }
}

/**
 * The Display class represents a display of test generation results in a project.
 * It implements the Runnable interface to run the display in a separate thread.
 *
 * @property project The project in which the display is shown.
 * @property numberOfUsedTool The number of tools used for the test generation.
 */
private class Display(private val project: Project, private val numberOfUsedTool: Int) : Runnable {
    private val log = Logger.getInstance(this::class.java)

    override fun run() {
        // waiting time after each iteration
        val sleepDurationMillis: Long = 1000

        // waiting for the generation result
        while (true) {
            // checks if all generator are finished their work
            if (project.service<TestGenerationDataService>().testGenerationResultList.size != numberOfUsedTool) {
                // there is some error during the process running
                if (project.service<ErrorService>().isErrorOccurred()) break
                log.info("Found ${project.service<TestGenerationDataService>().testGenerationResultList.size} number of results")
                log.info("Waiting for other generation results")
                Thread.sleep(sleepDurationMillis)
                continue
            }

            log.info("Found all $numberOfUsedTool generation results")

            ApplicationManager.getApplication().invokeLater {
                project.service<ReportLockingService>().receiveReport(getMergeResult(numberOfUsedTool))
            }

            break
        }

        project.service<RunnerService>().clear()
    }

    /**
     * Retrieves the merged result of a test generation process.
     *
     * @param numberOfUsedTool The number of tools used for the test generation.
     * @return The merged report containing the results of the test generation process.
     */
    private fun getMergeResult(numberOfUsedTool: Int): Report {
        log.info("Merging $numberOfUsedTool generation results")

        if (numberOfUsedTool == 1) {
            return project.service<TestGenerationDataService>().testGenerationResultList[0]!!
        }
        TODO("implement merge")
    }
}
