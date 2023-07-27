package org.jetbrains.research.testgenie.tools.llm.generation

import com.intellij.psi.PsiClass
import org.jetbrains.research.testgenie.actions.getMethodFullText
import org.jetbrains.research.testgenie.actions.getClassDisplayName
import org.jetbrains.research.testgenie.actions.getClassFullText
import org.jetbrains.research.testgenie.actions.getMethodName
import org.jetbrains.research.testgenie.actions.getSignatureString


/**
 * A class that manages prompts for generating unit tests.
 *
 * @constructor Creates a PromptManager with the given parameters.
 * @param cut The class under test.
 * @param classesToTest The classes to be tested.
 * @param interestingPsiClasses The set of interesting PsiClasses.
 * @param polymorphismRelations The map of polymorphism relations.
 */
class PromptManager(
    private val cut: PsiClass,
    private val classesToTest: MutableList<PsiClass>,
    private val interestingPsiClasses: MutableSet<PsiClass>,
    private val polymorphismRelations: MutableMap<PsiClass, MutableList<PsiClass>>,
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
        return "Generate unit tests in Java for ${getClassDisplayName(cut)} to achieve 100% line coverage for this class.\n" +
            header +
            "The source code of class under test is as follows:\n```\n${getClassFullText(cut)}\n```\n" +
            getCommonPromptPart()
    }

    /**
     * Generates a prompt for a method.
     *
     * @param methodDescriptor The descriptor of the method.
     * @return The generated prompt.
     */
    fun generatePromptForMethod(methodDescriptor: String): String {
        return "Generate unit tests in Java for ${getClassDisplayName(cut)} to achieve 100% line coverage for method $methodDescriptor.\n" +
            header +
            "The source code of method under test is as follows:\n```\n${getMethodFullText(cut, methodDescriptor)}\n```\n" +
            getCommonPromptPart()
    }

    /**
     * Generates a prompt for a specific line number in the code.
     *
     * @param lineNumber the line number for which to generate the prompt
     * @return the generated prompt string
     */
    fun generatePromptForLine(lineNumber: Int): String {
        return "Generate unit tests in Java for ${getClassDisplayName(cut)} only those that cover the line: `${getClassFullText(cut).split("\n")[lineNumber - 1]}` on line number $lineNumber.\n" +
            header +
            "The source code of method this the chosen line under test is as follows:\n```\n${getMethodFullText(cut, getMethodName(cut, lineNumber))}\n```\n" +
            getCommonPromptPart()
    }

    /**
     * Retrieves the common prompt part for generating test documentation.
     *
     * @return The common prompt part as a string.
     */
    private fun getCommonPromptPart(): String {
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
            val interestingPsiClassQN = interestingPsiClass.qualifiedName
            if (interestingPsiClassQN.equals(cut.qualifiedName)) {
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
}
