package org.jetbrains.research.testspark.tools.llm

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.jetbrains.research.testspark.actions.controllers.TestGenerationController
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.display.TestSparkDisplayBuilder
import org.jetbrains.research.testspark.helpers.LLMHelper
import org.jetbrains.research.testspark.helpers.psiHelpers.PsiHelperGetter
import org.jetbrains.research.testspark.tools.Pipeline
import org.jetbrains.research.testspark.tools.llm.generation.LLMProcessManager
import org.jetbrains.research.testspark.tools.llm.generation.PromptManager
import org.jetbrains.research.testspark.tools.template.Tool

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
    private fun getLLMProcessManager(project: Project, psiFile: PsiFile, caretOffset: Int, testSamplesCode: String): LLMProcessManager {
        val classesToTest = mutableListOf<PsiClass>()

        ApplicationManager.getApplication().runReadAction(
            Computable {
                collectClassesToTest(project, classesToTest, psiFile, caretOffset)
            },
        )

        return LLMProcessManager(
            project,
            PromptManager(project, classesToTest[0], classesToTest),
            testSamplesCode,
        )
    }

    /**
     * Fills the classesToTest variable with the data about the classes to test
     *
     * @param classesToTest The list of classes to test
     * @param psiFile The PSI file.
     * @param caretOffset The caret offset in the file.
     */
    private fun collectClassesToTest(project: Project, classesToTest: MutableList<PsiClass>, psiFile: PsiFile, caretOffset: Int) {
        // check if cut has any none java super class
        val maxPolymorphismDepth = SettingsArguments(project).maxPolyDepth(0)

        val cutPsiClass: PsiClass = PsiHelperGetter.getPsiHelper(psiFile).getSurroundingClass(psiFile, caretOffset)!!
        var currentPsiClass = cutPsiClass
        for (index in 0 until maxPolymorphismDepth) {
            if (!classesToTest.contains(currentPsiClass)) {
                classesToTest.add(currentPsiClass)
            }

            if (currentPsiClass.superClass == null ||
                currentPsiClass.superClass!!.qualifiedName == null ||
                currentPsiClass.superClass!!.qualifiedName!!.startsWith("java.")
            ) {
                break
            }
            currentPsiClass = currentPsiClass.superClass!!
        }
    }

    /**
     * Generates test cases for a class in the specified project.
     *
     * @param project The project containing the class.
     * @param psiFile The PSI file representation of the class.
     * @param caretOffset The caret offset in the class.
     * @param fileUrl The URL of the class file. It can be null.
     * @param testSamplesCode The code of the test samples.
     */
    override fun generateTestsForClass(project: Project, psiFile: PsiFile, caretOffset: Int, fileUrl: String?, testSamplesCode: String, testGenerationController: TestGenerationController, testSparkDisplayBuilder: TestSparkDisplayBuilder) {
        if (!LLMHelper.isCorrectToken(project, testGenerationController.errorMonitor)) {
            return
        }
        val codeType = FragmentToTestData(CodeType.CLASS)
        createLLMPipeline(project, psiFile, caretOffset, fileUrl, testGenerationController, testSparkDisplayBuilder).runTestGeneration(getLLMProcessManager(project, psiFile, caretOffset, testSamplesCode), codeType)
    }

    /**
     * Generates tests for a given method.
     *
     * @param project the project in which the method is located
     * @param psiFile the PSI file in which the method is located
     * @param caretOffset the offset of the caret position in the PSI file
     * @param fileUrl the URL of the file to generate tests for (optional)
     * @param testSamplesCode the code of the test samples to use for test generation
     */
    override fun generateTestsForMethod(project: Project, psiFile: PsiFile, caretOffset: Int, fileUrl: String?, testSamplesCode: String, testGenerationController: TestGenerationController, testSparkDisplayBuilder: TestSparkDisplayBuilder) {
        if (!LLMHelper.isCorrectToken(project, testGenerationController.errorMonitor)) {
            testGenerationController.finished()
            return
        }
        val psiMethod: PsiMethod = PsiHelperGetter.getPsiHelper(psiFile).getSurroundingMethod(psiFile, caretOffset)!!
        val codeType = FragmentToTestData(CodeType.METHOD, PsiHelperGetter.getPsiHelper(psiFile).generateMethodDescriptor(psiMethod))
        createLLMPipeline(project, psiFile, caretOffset, fileUrl, testGenerationController, testSparkDisplayBuilder).runTestGeneration(getLLMProcessManager(project, psiFile, caretOffset, testSamplesCode), codeType)
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
    override fun generateTestsForLine(project: Project, psiFile: PsiFile, caretOffset: Int, fileUrl: String?, testSamplesCode: String, testGenerationController: TestGenerationController, testSparkDisplayBuilder: TestSparkDisplayBuilder) {
        if (!LLMHelper.isCorrectToken(project, testGenerationController.errorMonitor)) {
            testGenerationController.finished()
            return
        }
        val selectedLine: Int = PsiHelperGetter.getPsiHelper(psiFile).getSurroundingLine(psiFile, caretOffset)?.plus(1)!!
        val codeType = FragmentToTestData(CodeType.LINE, selectedLine)
        createLLMPipeline(project, psiFile, caretOffset, fileUrl, testGenerationController, testSparkDisplayBuilder).runTestGeneration(getLLMProcessManager(project, psiFile, caretOffset, testSamplesCode), codeType)
    }

    /**
     * Creates a LLMPipeline instance.
     *
     * @param project the project of the pipeline
     * @param psiFile the PSI file associated with the pipeline
     * @param caretOffset the offset of the caret position within the PSI file
     * @param fileUrl the URL of the file to be processed by the pipeline
     * @return a LLMPipeline instance
     */
    private fun createLLMPipeline(project: Project, psiFile: PsiFile, caretOffset: Int, fileUrl: String?, testGenerationController: TestGenerationController, testSparkDisplayBuilder: TestSparkDisplayBuilder): Pipeline {
        val cutPsiClass: PsiClass = PsiHelperGetter.getPsiHelper(psiFile).getSurroundingClass(psiFile, caretOffset)!!

        val packageList = cutPsiClass.qualifiedName.toString().split(".").toMutableList()
        packageList.removeLast()

        val packageName = packageList.joinToString(".")

        return Pipeline(project, psiFile, caretOffset, fileUrl, packageName, testGenerationController, testSparkDisplayBuilder)
    }
}
