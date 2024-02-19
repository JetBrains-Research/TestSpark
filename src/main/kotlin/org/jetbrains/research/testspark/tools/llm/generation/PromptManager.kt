package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.core.generation.importPattern
import org.jetbrains.research.testspark.core.generation.packagePattern
import org.jetbrains.research.testspark.core.generation.prompt.PromptGenerator
import org.jetbrains.research.testspark.core.generation.prompt.configuration.GenerationSettings
import org.jetbrains.research.testspark.core.generation.prompt.configuration.PromptTemplates
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.core.helpers.generateMethodDescriptor
import org.jetbrains.research.testspark.services.PromptKeyword
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.services.TestGenerationDataService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager

/**
 * A class that manages prompts for generating unit tests.
 *
 * @constructor Creates a PromptManager with the given parameters.
 * @param cut The class under test.
 * @param classesToTest The classes to be tested.
 */
class PromptManager(
    private val project: Project,
    private val cut: PsiClass,
    private val classesToTest: MutableList<PsiClass>,
) {
    val settingsState: SettingsApplicationState = SettingsApplicationService.getInstance().state!!

    private val log = Logger.getInstance(this::class.java)
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()

    fun generatePrompt(codeType: FragmentToTestData): String {
        val promptGenerator = PromptGenerator(
            project,
            cut,
            classesToTest,
            GenerationSettings(
                maxInputParamsDepth = SettingsArguments.maxInputParamsDepth(project)
            ),
            PromptTemplates(
                classPrompt = settingsState.classPrompt,
                methodPrompt = settingsState.methodPrompt,
                linePrompt = settingsState.linePrompt,
            )
        )

        val prompt = ApplicationManager.getApplication().runReadAction(
            Computable {
                when (codeType.type!!) {
                    CodeType.CLASS -> promptGenerator.generatePromptForClass()
                    CodeType.METHOD -> promptGenerator.generatePromptForMethod(codeType.objectDescription)
                    CodeType.LINE -> promptGenerator.generatePromptForLine(codeType.objectIndex)
                }
            }
        )

        log.info("Prompt is:\n$prompt")
        return prompt
    }

    fun reducePromptSize(): Boolean {
        // reducing depth of polymorphism
        if (SettingsArguments.maxPolyDepth(project) > 1) {
            project.service<TestGenerationDataService>().polyDepthReducing++
            log.info("polymorphism depth is: ${SettingsArguments.maxPolyDepth(project)}")
            showPromptReductionWarning()
            return true
        }

        // reducing depth of input params
        if (SettingsArguments.maxInputParamsDepth(project) > 1) {
            project.service<TestGenerationDataService>().inputParamsDepthReducing++
            log.info("input params depth is: ${SettingsArguments.maxPolyDepth(project)}")
            showPromptReductionWarning()
            return true
        }

        return false
    }

    private fun showPromptReductionWarning() {
        llmErrorManager.warningProcess(
            TestSparkBundle.message("promptReduction") + "\n" +
                "Maximum depth of polymorphism is ${SettingsArguments.maxPolyDepth(project)}.\n" +
                "Maximum depth for input parameters is ${SettingsArguments.maxInputParamsDepth(project)}.",
            project,
        )
    }
}
