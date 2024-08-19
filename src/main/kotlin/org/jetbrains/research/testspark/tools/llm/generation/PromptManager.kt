package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.research.testspark.bundles.llm.LLMMessagesBundle
import org.jetbrains.research.testspark.bundles.llm.LLMSettingsBundle
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.generation.llm.prompt.PromptGenerator
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.ClassRepresentation
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.MethodRepresentation
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.PromptConfiguration
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.PromptGenerationContext
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.PromptTemplates
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.data.llm.JsonEncoding
import org.jetbrains.research.testspark.langwrappers.PsiClassWrapper
import org.jetbrains.research.testspark.langwrappers.PsiHelper
import org.jetbrains.research.testspark.langwrappers.PsiHelperProvider
import org.jetbrains.research.testspark.langwrappers.PsiMethodWrapper
import org.jetbrains.research.testspark.services.LLMSettingsService
import org.jetbrains.research.testspark.settings.llm.LLMSettingsState
import org.jetbrains.research.testspark.tools.llm.LlmSettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager

/**
 * A class that manages prompts for generating unit tests.
 *
 * @constructor Creates a PromptManager with the given parameters.
 * @param psiHelper The PsiHelper in the context of which the pipeline is executed.
 * @param caret The place of the caret.
 */
class PromptManager(
    private val project: Project,
    private val psiHelper: PsiHelper,
    private val caret: Int,
) {
    private val classesToTest: List<PsiClassWrapper>
        get() {
            val classesToTest = mutableListOf<PsiClassWrapper>()

            ApplicationManager.getApplication().runReadAction(
                Computable {
                    val maxPolymorphismDepth = LlmSettingsArguments(project).maxPolyDepth(polyDepthReducing = 0)
                    psiHelper.collectClassesToTest(project, classesToTest, caret, maxPolymorphismDepth)
                },
            )
            return classesToTest
        }

    private val cut: PsiClassWrapper = classesToTest[0]

    private val llmSettingsState: LLMSettingsState
        get() = project.getService(LLMSettingsService::class.java).state

    private val log = Logger.getInstance(this::class.java)
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()

    fun generatePrompt(codeType: FragmentToTestData, testSamplesCode: String, polyDepthReducing: Int): String {
        val prompt = ApplicationManager.getApplication().runReadAction(
            Computable {
                val maxInputParamsDepth = LlmSettingsArguments(project).maxInputParamsDepth(polyDepthReducing)
                val interestingPsiClasses =
                    psiHelper.getInterestingPsiClassesWithQualifiedNames(
                        project,
                        classesToTest,
                        polyDepthReducing,
                        maxInputParamsDepth,
                    )

                val interestingClasses = interestingPsiClasses.map(this::createClassRepresentation).toList()
                val polymorphismRelations =
                    getPolymorphismRelationsWithQualifiedNames(project, interestingPsiClasses)
                        .map(this::createClassRepresentation)
                        .toMap()

                val context = PromptGenerationContext(
                    cut = createClassRepresentation(cut),
                    classesToTest = classesToTest.map(this::createClassRepresentation).toList(),
                    polymorphismRelations = polymorphismRelations,
                    promptConfiguration = PromptConfiguration(
                        desiredLanguage = psiHelper.language.languageId,
                        desiredTestingPlatform = llmSettingsState.junitVersion.showName,
                        desiredMockingFramework = "Mockito 5",
                    ),
                )

                val promptTemplates = PromptTemplates(
                    classPrompt = JsonEncoding.decode(llmSettingsState.classPrompts)[llmSettingsState.classCurrentDefaultPromptIndex],
                    methodPrompt = JsonEncoding.decode(llmSettingsState.methodPrompts)[llmSettingsState.methodCurrentDefaultPromptIndex],
                    linePrompt = JsonEncoding.decode(llmSettingsState.linePrompts)[llmSettingsState.lineCurrentDefaultPromptIndex],
                )

                val promptGenerator = PromptGenerator(context, promptTemplates)

                when (codeType.type!!) {
                    CodeType.CLASS -> {
                        promptGenerator.generatePromptForClass(interestingClasses, testSamplesCode)
                    }

                    CodeType.METHOD -> {
                        val psiMethod = getPsiMethod(cut, codeType.objectDescription)!!
                        val method = createMethodRepresentation(psiMethod)!!
                        val interestingClassesFromMethod =
                            psiHelper.getInterestingPsiClassesWithQualifiedNames(cut, psiMethod)
                                .map(this::createClassRepresentation)
                                .toList()

                        promptGenerator.generatePromptForMethod(method, interestingClassesFromMethod, testSamplesCode)
                    }

                    CodeType.LINE -> {
                        val lineNumber = codeType.objectIndex
                        val psiMethod = getPsiMethod(cut, getMethodDescriptor(cut, lineNumber))!!

                        // get code of line under test
                        val document = PsiDocumentManager.getInstance(project).getDocument(cut.containingFile)
                        val lineStartOffset = document!!.getLineStartOffset(lineNumber - 1)
                        val lineEndOffset = document.getLineEndOffset(lineNumber - 1)

                        val lineUnderTest = document.getText(TextRange.create(lineStartOffset, lineEndOffset))
                        val method = createMethodRepresentation(psiMethod)!!
                        val interestingClassesFromMethod =
                            psiHelper.getInterestingPsiClassesWithQualifiedNames(cut, psiMethod)
                                .map(this::createClassRepresentation)
                                .toList()

                        promptGenerator.generatePromptForLine(
                            lineUnderTest,
                            method,
                            interestingClassesFromMethod,
                            testSamplesCode,
                        )
                    }
                }
            },
        ) + LLMSettingsBundle.get("commonPromptPart")
        log.info("Prompt is:\n$prompt")
        return prompt
    }

    private fun createMethodRepresentation(psiMethod: PsiMethodWrapper): MethodRepresentation? {
        psiMethod.text ?: return null
        return MethodRepresentation(
            signature = psiMethod.signature,
            name = psiMethod.name,
            text = psiMethod.text!!,
            containingClassQualifiedName = psiMethod.containingClass!!.qualifiedName,
        )
    }

    private fun createClassRepresentation(psiClass: PsiClassWrapper): ClassRepresentation {
        return ClassRepresentation(
            psiClass.qualifiedName,
            psiClass.fullText,
            psiClass.allMethods.map(this::createMethodRepresentation).toList().filterNotNull(),
            psiClass.classType,
        )
    }

    private fun createClassRepresentation(
        entry: Map.Entry<PsiClassWrapper, MutableList<PsiClassWrapper>>,
    ): Pair<ClassRepresentation, List<ClassRepresentation>> {
        val key = createClassRepresentation(entry.key)
        val value = entry.value.map(this::createClassRepresentation)

        return key to value // mapOf(key to value).entries.first()
    }

    fun isPromptSizeReductionPossible(testGenerationData: TestGenerationData): Boolean {
        return (LlmSettingsArguments(project).maxPolyDepth(testGenerationData.polyDepthReducing) > 1) ||
            (LlmSettingsArguments(project).maxInputParamsDepth(testGenerationData.inputParamsDepthReducing) > 1)
    }

    fun reducePromptSize(testGenerationData: TestGenerationData): Boolean {
        // reducing depth of polymorphism
        if (LlmSettingsArguments(project).maxPolyDepth(testGenerationData.polyDepthReducing) > 1) {
            testGenerationData.polyDepthReducing++
            log.info("polymorphism depth is: ${LlmSettingsArguments(project).maxPolyDepth(testGenerationData.polyDepthReducing)}")
            showPromptReductionWarning(testGenerationData)
            return true
        }

        // reducing depth of input params
        if (LlmSettingsArguments(project).maxInputParamsDepth(testGenerationData.inputParamsDepthReducing) > 1) {
            testGenerationData.inputParamsDepthReducing++
            log.info("input params depth is: ${LlmSettingsArguments(project).maxInputParamsDepth(testGenerationData.inputParamsDepthReducing)}")
            showPromptReductionWarning(testGenerationData)
            return true
        }

        return false
    }

    private fun showPromptReductionWarning(testGenerationData: TestGenerationData) {
        llmErrorManager.warningProcess(
            LLMMessagesBundle.get("promptReduction") + "\n" +
                "Maximum depth of polymorphism is ${LlmSettingsArguments(project).maxPolyDepth(testGenerationData.polyDepthReducing)}.\n" +
                "Maximum depth for input parameters is ${LlmSettingsArguments(project).maxInputParamsDepth(testGenerationData.inputParamsDepthReducing)}.",
            project,
        )
    }

    /**
     * Retrieves the polymorphism relations between a given set of interesting PsiClasses and a cut PsiClass.
     *
     * @param project The project context in which the PsiClasses exist.
     * @param interestingPsiClasses The set of PsiClassWrappers that are considered interesting.
     * @param cutPsiClass The cut PsiClassWrapper to determine polymorphism relations against.
     * @return A mutable map where the key represents an interesting PsiClass and the value is a list of its detected subclasses.
     */
    private fun getPolymorphismRelationsWithQualifiedNames(
        project: Project,
        interestingPsiClasses: MutableSet<PsiClassWrapper>,
    ): MutableMap<PsiClassWrapper, MutableList<PsiClassWrapper>> {
        val polymorphismRelations: MutableMap<PsiClassWrapper, MutableList<PsiClassWrapper>> = mutableMapOf()

        interestingPsiClasses.add(cut)

        interestingPsiClasses.forEach { currentInterestingClass ->
            val detectedSubClasses = currentInterestingClass.searchSubclasses(project)

            detectedSubClasses.forEach { detectedSubClass ->
                if (!polymorphismRelations.contains(currentInterestingClass)) {
                    polymorphismRelations[currentInterestingClass] = ArrayList()
                }
                polymorphismRelations[currentInterestingClass]?.add(detectedSubClass)
            }
        }

        interestingPsiClasses.remove(cut)

        return polymorphismRelations.toMutableMap()
    }

    /**
     * Retrieves a PsiMethod matching the given method descriptor within the provided PsiClass.
     *
     * @param psiClass The PsiClassWrapper in which to search for the method.
     * @param methodDescriptor The method descriptor to match against.
     * @return The matching PsiMethod if found, otherwise an empty string.
     */
    private fun getPsiMethod(
        psiClass: PsiClassWrapper,
        methodDescriptor: String,
    ): PsiMethodWrapper? {
        for (currentPsiMethod in psiClass.allMethods) {
            val file = psiClass.containingFile
            val psiHelper = PsiHelperProvider.getPsiHelper(file)
            // psiHelper will not be null here
            // because if we are here, then we already know that the current language is supported
            if (psiHelper!!.generateMethodDescriptor(currentPsiMethod) == methodDescriptor) {
                return currentPsiMethod
            }
        }
        return null
    }

    /**
     * Returns the method descriptor of the method containing the given line number in the specified PsiClass.
     *
     * @param psiClass the PsiClassWrapper containing the method
     * @param lineNumber the line number within the file where the method is located
     * @return the method descriptor as a String, or an empty string if no method is found
     */
    private fun getMethodDescriptor(
        psiClass: PsiClassWrapper,
        lineNumber: Int,
    ): String {
        for (currentPsiMethod in psiClass.allMethods) {
            if (currentPsiMethod.containsLine(lineNumber)) {
                val file = psiClass.containingFile
                val psiHelper = PsiHelperProvider.getPsiHelper(file)
                // psiHelper will not be null here
                // because if we are here, then we already know that the current language is supported
                return psiHelper!!.generateMethodDescriptor(currentPsiMethod)
            }
        }
        return ""
    }
}
