package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.research.testspark.actions.getClassFullText
import org.jetbrains.research.testspark.actions.getSignatureString
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.helpers.generateMethodDescriptor
import org.jetbrains.research.testspark.services.PROMPT_KEYWORD
import org.jetbrains.research.testspark.services.ProjectContextService
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import org.jetbrains.research.testspark.tools.isPromptLengthWithinLimit
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
        var prompt: String
        while (true) {
            prompt = when (codeType.type!!) {
                CodeType.CLASS -> generatePromptForClass()
                CodeType.METHOD -> generatePromptForMethod(codeType.objectDescription)
                CodeType.LINE -> generatePromptForLine(codeType.objectIndex)
            }

            // Too big prompt processing
            if (!isPromptLengthWithinLimit(prompt)) {
                // depth of polymorphism reducing
                if (SettingsArguments.maxPolyDepth(project) > 1) {
                    project.service<ProjectContextService>().testGenerationData.polyDepthReducing++
                    log.info("polymorphism depth is: ${SettingsArguments.maxPolyDepth(project)}")
                    continue
                }

                // depth of input params reducing
                if (SettingsArguments.maxInputParamsDepth(project) > 1) {
                    project.service<ProjectContextService>().testGenerationData.inputParamsDepthReducing++
                    log.info("input params depth is: ${SettingsArguments.maxPolyDepth(project)}")
                    continue
                }
            }
            break
        }

        // Show warning in case of depth reduction
        if ((
                project.service<ProjectContextService>().testGenerationData.polyDepthReducing != 0 ||
                    project.service<ProjectContextService>().testGenerationData.inputParamsDepthReducing != 0
                ) &&
            isPromptLengthWithinLimit(prompt)
        ) {
            llmErrorManager.warningProcess(
                TestSparkBundle.message("promptReduction") + "\n" +
                    "Maximum depth of polymorphism is ${SettingsArguments.maxPolyDepth(project)}.\n" +
                    "Maximum depth for input parameters is ${SettingsArguments.maxInputParamsDepth(project)}.",
                project,
            )
        }

        log.info("Prompt is:\n$prompt")
        return prompt
    }

    /**
     * Generates a prompt for generating unit tests in Java for a given class.
     *
     * @return The generated prompt.
     */
    private fun generatePromptForClass(): String {
        var classPrompt = settingsState.classPrompt
        val interestingPsiClasses = getInterestingPsiClasses(classesToTest)

        classPrompt = insertLanguage(classPrompt)
        classPrompt = insertName(classPrompt, cut.qualifiedName!!)
        classPrompt = insertTestingPlatform(classPrompt)
        classPrompt = insertMockingFramework(classPrompt)
        classPrompt = insertCodeUnderTest(classPrompt, getClassFullText(cut))
        classPrompt = insertMethodsSignatures(classPrompt, interestingPsiClasses)
        classPrompt =
            insertPolymorphismRelations(classPrompt, getPolymorphismRelations(project, interestingPsiClasses, cut))

        return classPrompt
    }

    /**
     * Generates a prompt for a method.
     *
     * @param methodDescriptor The descriptor of the method.
     * @return The generated prompt.
     */
    private fun generatePromptForMethod(methodDescriptor: String): String {
        var methodPrompt = settingsState.methodPrompt
        val psiMethod = getPsiMethod(cut, methodDescriptor)!!

        methodPrompt = insertLanguage(methodPrompt)
        methodPrompt = insertName(methodPrompt, "${cut.qualifiedName!!}.${psiMethod.name}")
        methodPrompt = insertTestingPlatform(methodPrompt)
        methodPrompt = insertMockingFramework(methodPrompt)
        methodPrompt = insertCodeUnderTest(methodPrompt, psiMethod.text)
        methodPrompt = insertMethodsSignatures(methodPrompt, getInterestingPsiClasses(psiMethod))
        methodPrompt = insertPolymorphismRelations(
            methodPrompt,
            getPolymorphismRelations(project, getInterestingPsiClasses(classesToTest), cut),
        )

        return methodPrompt
    }

    /**
     * Generates a prompt for a specific line number in the code.
     *
     * @param lineNumber the line number for which to generate the prompt
     * @return the generated prompt string
     */
    private fun generatePromptForLine(lineNumber: Int): String {
        var linePrompt = settingsState.linePrompt
        val methodDescriptor = getMethodDescriptor(cut, lineNumber)
        val psiMethod = getPsiMethod(cut, methodDescriptor)!!

        // get code of line under test
        val document = PsiDocumentManager.getInstance(project).getDocument(cut.containingFile)
        val lineStartOffset = document!!.getLineStartOffset(lineNumber - 1)
        val lineEndOffset = document.getLineEndOffset(lineNumber - 1)
        val lineUnderTest = document.getText(TextRange.create(lineStartOffset, lineEndOffset))

        linePrompt = insertLanguage(linePrompt)
        linePrompt = insertName(linePrompt, lineUnderTest.trim())
        linePrompt = insertTestingPlatform(linePrompt)
        linePrompt = insertMockingFramework(linePrompt)
        linePrompt = insertCodeUnderTest(linePrompt, psiMethod.text)
        linePrompt = insertMethodsSignatures(linePrompt, getInterestingPsiClasses(psiMethod))
        linePrompt = insertPolymorphismRelations(
            linePrompt,
            getPolymorphismRelations(project, getInterestingPsiClasses(classesToTest), cut),
        )

        return linePrompt
    }

    private fun isPromptValid(keyword: PROMPT_KEYWORD, prompt: String): Boolean {
        val keywordText = keyword.text
        val isMandatory = keyword.mandatory

        return (prompt.contains(keywordText) || !isMandatory)
    }

    private fun insertLanguage(classPrompt: String): String {
        if (isPromptValid(PROMPT_KEYWORD.LANGUAGE, classPrompt)) {
            val keyword = "\$${PROMPT_KEYWORD.LANGUAGE.text}"
            return classPrompt.replace(keyword, "Java", ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PROMPT_KEYWORD.LANGUAGE.text}")
        }
    }

    private fun insertName(classPrompt: String, classDisplayName: String): String {
        if (isPromptValid(PROMPT_KEYWORD.NAME, classPrompt)) {
            val keyword = "\$${PROMPT_KEYWORD.NAME.text}"
            return classPrompt.replace(keyword, classDisplayName, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PROMPT_KEYWORD.NAME.text}")
        }
    }

    private fun insertTestingPlatform(classPrompt: String): String {
        if (isPromptValid(PROMPT_KEYWORD.TESTING_PLATFORM, classPrompt)) {
            val keyword = "\$${PROMPT_KEYWORD.TESTING_PLATFORM.text}"
            return classPrompt.replace(keyword, "JUnit 4", ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PROMPT_KEYWORD.TESTING_PLATFORM.text}")
        }
    }

    private fun insertMockingFramework(classPrompt: String): String {
        if (isPromptValid(PROMPT_KEYWORD.MOCKING_FRAMEWORK, classPrompt)) {
            val keyword = "\$${PROMPT_KEYWORD.MOCKING_FRAMEWORK.text}"
            return classPrompt.replace(keyword, "Mockito 5", ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PROMPT_KEYWORD.MOCKING_FRAMEWORK.text}")
        }
    }

    private fun insertCodeUnderTest(classPrompt: String, classFullText: String): String {
        if (isPromptValid(PROMPT_KEYWORD.CODE, classPrompt)) {
            val keyword = "\$${PROMPT_KEYWORD.CODE.text}"
            var fullText = "```\n${classFullText}\n```\n"

            for (i in 2..classesToTest.size) {
                val subClass = classesToTest[i - 2]
                val superClass = classesToTest[i - 1]

                fullText += "${subClass.qualifiedName} extends ${superClass.qualifiedName}. " +
                    "The source code of ${superClass.qualifiedName} is:\n```\n${getClassFullText(superClass)}\n" +
                    "```\n"
            }
            return classPrompt.replace(keyword, fullText, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PROMPT_KEYWORD.CODE.text}")
        }
    }

    private fun insertMethodsSignatures(classPrompt: String, interestingPsiClasses: MutableSet<PsiClass>): String {
        val keyword = "\$${PROMPT_KEYWORD.METHODS.text}"

        if (isPromptValid(PROMPT_KEYWORD.METHODS, classPrompt)) {
            var fullText = ""
            for (interestingPsiClass: PsiClass in interestingPsiClasses) {
                if (interestingPsiClass.qualifiedName!!.startsWith("java")) {
                    continue
                }

                fullText += "=== methods in ${interestingPsiClass.qualifiedName!!}:\n"
                for (currentPsiMethod in interestingPsiClass.allMethods) {
                    // Skip java methods
                    if (currentPsiMethod.containingClass!!.qualifiedName!!.startsWith("java")) {
                        continue
                    }
                    fullText += " - ${currentPsiMethod.getSignatureString()}\n"
                }
            }
            return classPrompt.replace(keyword, fullText, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PROMPT_KEYWORD.METHODS.text}")
        }
    }

    private fun insertPolymorphismRelations(
        classPrompt: String,
        polymorphismRelations: MutableMap<PsiClass, MutableList<PsiClass>>,
    ): String {
        val keyword = "\$${PROMPT_KEYWORD.POLYMORPHISM.text}"
        if (isPromptValid(PROMPT_KEYWORD.METHODS, classPrompt)) {
            var fullText = ""

            polymorphismRelations.forEach { entry ->
                for (currentSubClass in entry.value) {
                    currentSubClass.qualifiedName ?: continue
                    fullText += "${currentSubClass.qualifiedName} is a sub-class of ${entry.key.qualifiedName}.\n"
                }
            }
            return classPrompt.replace(keyword, fullText, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PROMPT_KEYWORD.POLYMORPHISM.text}")
        }
    }

    /**
     * Returns a set of interesting PsiClasses based on the given PsiMethod.
     *
     * @param psiMethod the PsiMethod for which to find interesting PsiClasses
     * @return a mutable set of interesting PsiClasses
     */
    private fun getInterestingPsiClasses(psiMethod: PsiMethod): MutableSet<PsiClass> {
        val interestingMethods = mutableSetOf(psiMethod)
        for (currentPsiMethod in cut.allMethods) {
            if (currentPsiMethod.isConstructor) interestingMethods.add(currentPsiMethod)
        }
        val interestingPsiClasses = mutableSetOf(cut)
        interestingMethods.forEach { methodIt ->
            methodIt.parameterList.parameters.forEach { paramIt ->
                PsiTypesUtil.getPsiClass(paramIt.type)?.let {
                    if (it.qualifiedName != null && !it.qualifiedName!!.startsWith("java.")) {
                        interestingPsiClasses.add(it)
                    }
                }
            }
        }
        return interestingPsiClasses
    }

    /**
     * Retrieves a set of interesting PsiClasses based on a given cutPsiClass and a list of classesToTest.
     *
     * @param classesToTest The list of classes to test for interesting PsiClasses.
     * @return The set of interesting PsiClasses found during the search.
     */
    private fun getInterestingPsiClasses(classesToTest: MutableList<PsiClass>): MutableSet<PsiClass> {
        val interestingPsiClasses: MutableSet<PsiClass> = mutableSetOf()

        var currentLevelClasses = mutableListOf<PsiClass>().apply { addAll(classesToTest) }

        repeat(SettingsArguments.maxInputParamsDepth(project)) {
            val tempListOfClasses = mutableSetOf<PsiClass>()

            currentLevelClasses.forEach { classIt ->
                classIt.methods.forEach { methodIt ->
                    methodIt.parameterList.parameters.forEach { paramIt ->
                        PsiTypesUtil.getPsiClass(paramIt.type)?.let {
                            if (!interestingPsiClasses.contains(it) && it.qualifiedName != null &&
                                !it.qualifiedName!!.startsWith("java.")
                            ) {
                                tempListOfClasses.add(it)
                            }
                        }
                    }
                }
            }
            currentLevelClasses = mutableListOf<PsiClass>().apply { addAll(tempListOfClasses) }
            interestingPsiClasses.addAll(tempListOfClasses)
        }

        return interestingPsiClasses
    }

    /**
     * Retrieves the polymorphism relations between a given set of interesting PsiClasses and a cut PsiClass.
     *
     * @param project The project context in which the PsiClasses exist.
     * @param interestingPsiClasses The set of PsiClasses that are considered interesting.
     * @param cutPsiClass The cut PsiClass to determine polymorphism relations against.
     * @return A mutable map where the key represents an interesting PsiClass and the value is a list of its detected subclasses.
     */
    private fun getPolymorphismRelations(
        project: Project,
        interestingPsiClasses: MutableSet<PsiClass>,
        cutPsiClass: PsiClass,
    ): MutableMap<PsiClass, MutableList<PsiClass>> {
        val polymorphismRelations: MutableMap<PsiClass, MutableList<PsiClass>> = mutableMapOf()

        val psiClassesToVisit: ArrayDeque<PsiClass> = ArrayDeque(listOf(cutPsiClass))
        interestingPsiClasses.add(cutPsiClass)

        interestingPsiClasses.forEach { currentInterestingClass ->
            val scope = GlobalSearchScope.projectScope(project)
            val query = ClassInheritorsSearch.search(currentInterestingClass, scope, false)
            val detectedSubClasses: Collection<PsiClass> = query.findAll()

            detectedSubClasses.forEach { detectedSubClass ->
                if (!polymorphismRelations.contains(currentInterestingClass)) {
                    polymorphismRelations[currentInterestingClass] = ArrayList()
                }
                polymorphismRelations[currentInterestingClass]?.add(detectedSubClass)
                if (!psiClassesToVisit.contains(detectedSubClass)) {
                    psiClassesToVisit.addLast(detectedSubClass)
                }
            }
        }

        return polymorphismRelations
    }

    /**
     * Retrieves a PsiMethod matching the given method descriptor within the provided PsiClass.
     *
     * @param psiClass The PsiClass in which to search for the method.
     * @param methodDescriptor The method descriptor to match against.
     * @return The matching PsiMethod if found, otherwise an empty string.
     */
    private fun getPsiMethod(psiClass: PsiClass, methodDescriptor: String): PsiMethod? {
        for (currentPsiMethod in psiClass.allMethods) {
            if (generateMethodDescriptor(currentPsiMethod) == methodDescriptor) return currentPsiMethod
        }
        return null
    }

    /**
     * Returns the method descriptor of the method containing the given line number in the specified PsiClass.
     *
     * @param psiClass the PsiClass containing the method
     * @param lineNumber the line number within the file where the method is located
     * @return the method descriptor as a String, or an empty string if no method is found
     */
    private fun getMethodDescriptor(psiClass: PsiClass, lineNumber: Int): String {
        for (currentPsiMethod in psiClass.allMethods) {
            if (isLineInPsiMethod(currentPsiMethod, lineNumber)) return generateMethodDescriptor(currentPsiMethod)
        }
        return ""
    }

    /**
     * Checks if the given line number is within the range of the specified PsiMethod.
     *
     * @param method The PsiMethod to check.
     * @param lineNumber The line number to check.
     * @return `true` if the line number is within the range of the method, `false` otherwise.
     */
    private fun isLineInPsiMethod(method: PsiMethod, lineNumber: Int): Boolean {
        val psiFile = method.containingFile ?: return false
        val document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return false
        val textRange = method.textRange
        val startLine = document.getLineNumber(textRange.startOffset) + 1
        val endLine = document.getLineNumber(textRange.endOffset) + 1
        return lineNumber in startLine..endLine
    }
}
