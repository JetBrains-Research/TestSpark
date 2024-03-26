package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.application.ApplicationManager
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
import org.jetbrains.research.testspark.bundles.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.generation.llm.prompt.PromptGenerator
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.ClassRepresentation
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.MethodRepresentation
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.PromptConfiguration
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.PromptGenerationContext
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.PromptTemplates
import org.jetbrains.research.testspark.core.utils.importPattern
import org.jetbrains.research.testspark.core.utils.packagePattern
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.data.JsonEncoding
import org.jetbrains.research.testspark.helpers.generateMethodDescriptor
import org.jetbrains.research.testspark.services.SettingsApplicationService
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
    private val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

    private val log = Logger.getInstance(this::class.java)
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()

    fun generatePrompt(codeType: FragmentToTestData, testSamplesCode: String, polyDepthReducing: Int): String {
        val prompt = ApplicationManager.getApplication().runReadAction(
            Computable {
                val interestingPsiClasses = getInterestingPsiClasses(classesToTest, polyDepthReducing)

                val interestingClasses = interestingPsiClasses.map(this::createClassRepresentation).toList().filterNotNull()
                val polymorphismRelations = getPolymorphismRelations(project, interestingPsiClasses, cut)
                    .map(this::createClassRepresentation).toMap().filter { it.key != null }

                val context = PromptGenerationContext(
                    cut = createClassRepresentation(cut)!!,
                    classesToTest = classesToTest.map(this::createClassRepresentation).toList().filterNotNull(),
                    polymorphismRelations = polymorphismRelations,
                    promptConfiguration = PromptConfiguration(
                        desiredLanguage = "Java",
                        desiredTestingPlatform = settingsState.junitVersion.showName,
                        desiredMockingFramework = "Mockito 5",
                    ),
                )

                val promptTemplates = PromptTemplates(
                    classPrompt = JsonEncoding.decode(settingsState.classPrompt)[settingsState.currentClassTemplateNumber - 1],
                    methodPrompt = JsonEncoding.decode(settingsState.methodPrompt)[settingsState.currentMethodTemplateNumber - 1],
                    linePrompt = JsonEncoding.decode(settingsState.linePrompt)[settingsState.currentLineTemplateNumber - 1],
                )

                val promptGenerator = PromptGenerator(context, promptTemplates)

                when (codeType.type!!) {
                    CodeType.CLASS -> {
                        promptGenerator.generatePromptForClass(interestingClasses, testSamplesCode)
                    }
                    CodeType.METHOD -> {
                        val psiMethod = getPsiMethod(cut, codeType.objectDescription)!!
                        val method = createMethodRepresentation(psiMethod)!!
                        val interestingClassesFromMethod = getInterestingPsiClasses(psiMethod).map(this::createClassRepresentation).toList().filterNotNull()

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
                        val interestingClassesFromMethod = getInterestingPsiClasses(psiMethod).map(this::createClassRepresentation).toList().filterNotNull()

                        promptGenerator.generatePromptForLine(lineUnderTest, method, interestingClassesFromMethod, testSamplesCode)
                    }
                }
            },
        ) + TestSparkToolTipsBundle.defaultValue("commonPromptPart")
        log.info("Prompt is:\n$prompt")
        return prompt
    }

    private fun createMethodRepresentation(psiMethod: PsiMethod): MethodRepresentation? {
        psiMethod.text ?: return null
        return MethodRepresentation(
            signature = psiMethod.getSignatureString(),
            name = psiMethod.name,
            text = psiMethod.text,
            containingClassQualifiedName = psiMethod.containingClass!!.qualifiedName!!,
        )
    }

    private fun createClassRepresentation(psiClass: PsiClass): ClassRepresentation? {
        psiClass.qualifiedName ?: return null
        return ClassRepresentation(
            psiClass.qualifiedName,
            getClassFullText(psiClass),
            psiClass.allMethods.map(this::createMethodRepresentation).toList().filterNotNull(),
        )
    }

    private fun createClassRepresentation(
        entry: Map.Entry<PsiClass, MutableList<PsiClass>>,
    ): Pair<ClassRepresentation?, List<ClassRepresentation?>> {
        val key = createClassRepresentation(entry.key)
        val value = entry.value.map(this::createClassRepresentation)

        return key to value // mapOf(key to value).entries.first()
    }

    fun isPromptSizeReductionPossible(testGenerationData: TestGenerationData): Boolean {
        return (SettingsArguments.maxPolyDepth(testGenerationData.polyDepthReducing) > 1) ||
            (SettingsArguments.maxInputParamsDepth(testGenerationData.inputParamsDepthReducing) > 1)
    }

    fun reducePromptSize(testGenerationData: TestGenerationData): Boolean {
        // reducing depth of polymorphism
        if (SettingsArguments.maxPolyDepth(testGenerationData.polyDepthReducing) > 1) {
            testGenerationData.polyDepthReducing++
            log.info("polymorphism depth is: ${SettingsArguments.maxPolyDepth(testGenerationData.polyDepthReducing)}")
            showPromptReductionWarning(testGenerationData)
            return true
        }

        // reducing depth of input params
        if (SettingsArguments.maxInputParamsDepth(testGenerationData.inputParamsDepthReducing) > 1) {
            testGenerationData.inputParamsDepthReducing++
            log.info("input params depth is: ${SettingsArguments.maxInputParamsDepth(testGenerationData.inputParamsDepthReducing)}")
            showPromptReductionWarning(testGenerationData)
            return true
        }

        return false
    }

    private fun showPromptReductionWarning(testGenerationData: TestGenerationData) {
        llmErrorManager.warningProcess(
            TestSparkBundle.message("promptReduction") + "\n" +
                "Maximum depth of polymorphism is ${SettingsArguments.maxPolyDepth(testGenerationData.polyDepthReducing)}.\n" +
                "Maximum depth for input parameters is ${SettingsArguments.maxInputParamsDepth(testGenerationData.inputParamsDepthReducing)}.",
            project,
        )
    }

    private fun PsiMethod.getSignatureString(): String {
        val bodyStart = body?.startOffsetInParent ?: this.textLength
        return text.substring(0, bodyStart).replace('\n', ' ').trim()
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
    private fun getInterestingPsiClasses(classesToTest: MutableList<PsiClass>, polyDepthReducing: Int): MutableSet<PsiClass> {
        val interestingPsiClasses: MutableSet<PsiClass> = mutableSetOf()

        var currentLevelClasses = mutableListOf<PsiClass>().apply { addAll(classesToTest) }

        repeat(SettingsArguments.maxInputParamsDepth(polyDepthReducing)) {
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
    private fun getPsiMethod(
        psiClass: PsiClass,
        methodDescriptor: String,
    ): PsiMethod? {
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
    private fun getMethodDescriptor(
        psiClass: PsiClass,
        lineNumber: Int,
    ): String {
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
    private fun isLineInPsiMethod(
        method: PsiMethod,
        lineNumber: Int,
    ): Boolean {
        val psiFile = method.containingFile ?: return false
        val document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return false
        val textRange = method.textRange
        val startLine = document.getLineNumber(textRange.startOffset) + 1
        val endLine = document.getLineNumber(textRange.endOffset) + 1
        return lineNumber in startLine..endLine
    }

    /**
     * Returns the full text of a given class including the package, imports, and class code.
     *
     * @param cl The PsiClass object representing the class.
     * @return The full text of the class.
     */
    private fun getClassFullText(cl: PsiClass): String {
        var fullText = ""
        val fileText = cl.containingFile.text

        // get package
        packagePattern.findAll(fileText, 0).map {
            it.groupValues[0]
        }.forEach {
            fullText += "$it\n\n"
        }

        // get imports
        importPattern.findAll(fileText, 0).map {
            it.groupValues[0]
        }.forEach {
            fullText += "$it\n"
        }

        // Add class code
        fullText += cl.text

        return fullText
    }
}
