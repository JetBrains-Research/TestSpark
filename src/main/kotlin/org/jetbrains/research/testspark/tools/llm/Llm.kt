package org.jetbrains.research.testspark.tools.llm

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.actions.controllers.TestGenerationController
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.helpers.LLMHelper
import org.jetbrains.research.testspark.langwrappers.PsiClassWrapper
import org.jetbrains.research.testspark.langwrappers.PsiHelper
import org.jetbrains.research.testspark.tools.Pipeline
import org.jetbrains.research.testspark.tools.llm.generation.LLMProcessManager
import org.jetbrains.research.testspark.tools.llm.generation.PromptManager
import org.jetbrains.research.testspark.tools.template.Tool
import java.nio.file.Path

/**
 * The Llm class represents a tool called "Llm" that is used to generate tests for Java code.
 *
 * @param name The name of the tool. Default value is "Llm".
 */
class Llm(override val name: String = "LLM") : Tool {

    /**
     * Returns an instance of the LLMProcessManager.
     *
     * @param project The current project.
     * @param psiFile The PSI file.
     * @param caretOffset The caret offset in the file.
     * @param testSamplesCode The test samples code.
     * @return An instance of LLMProcessManager.
     */
    fun getLLMProcessManager(
        project: Project,
        psiHelper: PsiHelper,
        caretOffset: Int,
        testSamplesCode: String,
        projectSDKPath: Path? = null,
    ): LLMProcessManager {
        val classesToTest = mutableListOf<PsiClassWrapper>()
        val maxPolymorphismDepth = LlmSettingsArguments(project).maxPolyDepth(polyDepthReducing = 0)

        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            ApplicationManager.getApplication().runReadAction {
                psiHelper.collectClassesToTest(project, classesToTest, caretOffset, maxPolymorphismDepth)
            }
        }, PluginMessagesBundle.get("collectingClassesToTest"), false, project)

        return LLMProcessManager(
            project,
            psiHelper.language,
            PromptManager(project, psiHelper, caretOffset),
            testSamplesCode,
            projectSDKPath,
        )
    }

    /**
     * Generates test cases for a class in the specified project.
     *
     * @param project The project containing the class.
     * @param psiHelper the PsiHelper associated with the pipeline.
     * @param caretOffset The caret offset in the class.
     * @param fileUrl The URL of the class file. It can be null.
     * @param testSamplesCode The code of the test samples.
     */
    override fun generateTestsForClass(
        project: Project,
        psiHelper: PsiHelper,
        caretOffset: Int,
        fileUrl: String?,
        testSamplesCode: String,
        testGenerationController: TestGenerationController,
    ) {
        if (!LLMHelper.isCorrectToken(project, testGenerationController.errorMonitor)) {
            testGenerationController.finished()
            return
        }
        val codeType = FragmentToTestData(CodeType.CLASS)
        createLLMPipeline(project, psiHelper, caretOffset, fileUrl, testGenerationController).runTestGeneration(
            LLMProcessManager(
                project,
                psiHelper.language,
                PromptManager(project, psiHelper, caretOffset),
                testSamplesCode,
            ),
            codeType,
        )
    }

    /**
     * Generates tests for a given method.
     *
     * @param project the project in which the method is located.
     * @param psiHelper the PsiHelper associated with the pipeline.
     * @param caretOffset the offset of the caret position in the PSI file.
     * @param fileUrl the URL of the file to generate tests for (optional).
     * @param testSamplesCode the code of the test samples to use for test generation.
     */
    override fun generateTestsForMethod(
        project: Project,
        psiHelper: PsiHelper,
        caretOffset: Int,
        fileUrl: String?,
        testSamplesCode: String,
        testGenerationController: TestGenerationController,
    ) {
        if (!LLMHelper.isCorrectToken(project, testGenerationController.errorMonitor)) {
            testGenerationController.finished()
            return
        }
        val psiMethod = psiHelper.getSurroundingMethod(caretOffset)!!
        val codeType = FragmentToTestData(CodeType.METHOD, psiHelper.generateMethodDescriptor(psiMethod))
        createLLMPipeline(project, psiHelper, caretOffset, fileUrl, testGenerationController).runTestGeneration(
            LLMProcessManager(
                project,
                psiHelper.language,
                PromptManager(project, psiHelper, caretOffset),
                testSamplesCode,
            ),
            codeType,
        )
    }

    /**
     * Generates tests for a specific line of code.
     *
     * @param project The current project.
     * @param psiFile The PSI file containing the code.
     * @param caretOffset The offset position of the caret.
     * @param fileUrl The URL of the file.
     * @param testSamplesCode The code for the test samples.
     */
    override fun generateTestsForLine(
        project: Project,
        psiHelper: PsiHelper,
        caretOffset: Int,
        fileUrl: String?,
        testSamplesCode: String,
        testGenerationController: TestGenerationController,
    ) {
        if (!LLMHelper.isCorrectToken(project, testGenerationController.errorMonitor)) {
            testGenerationController.finished()
            return
        }
        val selectedLine: Int = psiHelper.getSurroundingLine(caretOffset)!!
        val codeType = FragmentToTestData(CodeType.LINE, selectedLine)
        createLLMPipeline(project, psiHelper, caretOffset, fileUrl, testGenerationController).runTestGeneration(
            LLMProcessManager(
                project,
                psiHelper.language,
                PromptManager(project, psiHelper, caretOffset),
                testSamplesCode,
            ),
            codeType,
        )
    }

    /**
     * Creates a LLMPipeline instance.
     *
     * @param project the project of the pipeline.
     * @param psiHelper the PsiHelper associated with the pipeline.
     * @param caretOffset the offset of the caret position within the PSI file.
     * @param fileUrl the URL of the file to be processed by the pipeline.
     * @return a LLMPipeline instance.
     */
    private fun createLLMPipeline(
        project: Project,
        psiHelper: PsiHelper,
        caretOffset: Int,
        fileUrl: String?,
        testGenerationController: TestGenerationController,
    ): Pipeline {
        val cutPsiClass = psiHelper.getSurroundingClass(caretOffset)!!
        val packageList = cutPsiClass.qualifiedName.split(".").dropLast(1)
        val packageName = packageList.joinToString(".")
        return Pipeline(project, psiHelper, caretOffset, fileUrl, packageName, testGenerationController)
    }
}
