package org.jetbrains.research.testgenie.tools.llm.generation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.research.testgenie.actions.getClassDisplayName
import org.jetbrains.research.testgenie.actions.getClassFullText
import org.jetbrains.research.testgenie.actions.getSignatureString
import org.jetbrains.research.testgenie.helpers.generateMethodDescriptor
import org.jetbrains.research.testgenie.tools.llm.SettingsArguments

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
    // prompt: start the request
    private val header = "Dont use @Before and @After test methods.\n" +
        "Make tests as atomic as possible.\n" +
        "All tests should be for JUnit 4.\n" +
        "In case of mocking, use Mockito 5. But, do not use mocking for all tests.\n"

    /**
     * Generates a prompt for generating unit tests in Java for a given class.
     *
     * @return The generated prompt.
     */
    fun generatePromptForClass(): String {
        val interestingPsiClasses = getInterestingPsiClasses(classesToTest)
        return "Generate unit tests in Java for ${getClassDisplayName(cut)} to achieve 100% line coverage for this class.\n" +
            header +
            "The source code of class under test is as follows:\n```\n${getClassFullText(cut)}\n```\n" +
            getCommonPromptPart(interestingPsiClasses, getPolymorphismRelations(project, interestingPsiClasses, cut))
    }

    /**
     * Generates a prompt for a method.
     *
     * @param methodDescriptor The descriptor of the method.
     * @return The generated prompt.
     */
    fun generatePromptForMethod(methodDescriptor: String): String {
        val psiMethod = getPsiMethod(cut, methodDescriptor)!!
        return "Generate unit tests in Java for ${getClassDisplayName(cut)} to achieve 100% line coverage for method $methodDescriptor.\n" +
            header +
            "The source code of method under test is as follows:\n```\n${psiMethod.text}\n```\n" +
            getCommonPromptPart(getInterestingPsiClasses(psiMethod))
    }

    /**
     * Generates a prompt for a specific line number in the code.
     *
     * @param lineNumber the line number for which to generate the prompt
     * @return the generated prompt string
     */
    fun generatePromptForLine(lineNumber: Int): String {
        val methodDescriptor = getMethodDescriptor(cut, lineNumber)
        val psiMethod = getPsiMethod(cut, methodDescriptor)!!
        return "Generate unit tests in Java for ${getClassDisplayName(cut)} only those that cover the line: `${getClassFullText(cut).split("\n")[lineNumber - 1]}` on line number $lineNumber.\n" +
            header +
            "The source code of method this the chosen line under test is as follows:\n```\n${psiMethod.text}\n```\n" +
            getCommonPromptPart(getInterestingPsiClasses(psiMethod))
    }

    /**
     * Retrieves the common prompt part for generating test documentation.
     *
     * @return The common prompt part as a string.
     */
    private fun getCommonPromptPart(interestingPsiClasses: MutableSet<PsiClass>, polymorphismRelations: MutableMap<PsiClass, MutableList<PsiClass>> = mutableMapOf()): String {
        var prompt = ""

        // print information about  super classes of CUT
        for (i in 2..classesToTest.size) {
            val subClass = classesToTest[i - 2]
            val superClass = classesToTest[i - 1]

            prompt += "${getClassDisplayName(subClass)} extends ${getClassDisplayName(superClass)}. " +
                "The source code of ${getClassDisplayName(superClass)} is:\n```\n${getClassFullText(superClass)}\n" +
                "```\n"
        }

        // prompt: signature of methods in the classes used by CUT
        prompt += "Here are the method signatures of classes used by the class under test. Only use these signatures for creating objects, not your own ideas.\n"
        for (interestingPsiClass: PsiClass in interestingPsiClasses) {
            if (interestingPsiClass.qualifiedName!!.startsWith("java")) {
                continue
            }

            prompt += "=== methods in ${getClassDisplayName(interestingPsiClass)}:\n"
            for (currentPsiMethod in interestingPsiClass.allMethods) {
                // Skip java methods
                if (currentPsiMethod.containingClass!!.qualifiedName!!.startsWith("java")) {
                    continue
                }
                prompt += " - ${currentPsiMethod.getSignatureString()}\n"
            }
            prompt += "\n\n"
        }

        // prompt: add polymorphism relations between involved classes
        prompt += "=== polymorphism relations:\n"
        polymorphismRelations.forEach { entry ->
            for (currentSubClass in entry.value) {
                prompt += "${currentSubClass.qualifiedName} is a sub-class of ${entry.key.qualifiedName}.\n"
            }
        }
        // Make sure that LLM does not provide extra information other than the test file
        prompt += "put the generated test between ```"

        return prompt
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
     * @param cutPsiClass The PsiClass to start the search from.
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
    private fun getPolymorphismRelations(project: Project, interestingPsiClasses: MutableSet<PsiClass>, cutPsiClass: PsiClass): MutableMap<PsiClass, MutableList<PsiClass>> {
        val polymorphismRelations: MutableMap<PsiClass, MutableList<PsiClass>> = mutableMapOf()

        val psiClassesToVisit: ArrayDeque<PsiClass> = ArrayDeque(listOf(cutPsiClass))

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
