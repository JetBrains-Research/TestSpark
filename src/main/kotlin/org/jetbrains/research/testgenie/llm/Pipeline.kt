package org.jetbrains.research.testgenie.llm

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.jetbrains.research.testgenie.actions.getSignatureString

private var prompt = "";
private val llmRequest = LLMRequest()


class Pipeline(
    private val project: Project,
    private val interestingPsiClasses: Set<PsiClass>,
    private val cut: PsiClass,
    private val polymorphismRelations: MutableMap<PsiClass, MutableList<PsiClass>>,
    private val modTs: Long,
) {

    // TODO("Removed unused input parameters. needs o be refactored after finalizing the implementation")

    fun forClass(): Pipeline {
        prompt = generatePrompt()
        return this
    }

    private fun generatePrompt(): String {
        // prompt: start the request
        var prompt =
            "Generate unit tests in Java for class ${cut.qualifiedName} to achieve 100% line coverage for this class.\nDont use @Before and @After test methods.\n"

        // prompt: source code
        prompt += "The source code of class under test is as follows:\n ${cut.text}\n"

        // prompt: signature of methods in the classes used by CUT
        prompt += "Here are the method signatures of classes used by the class under test. Only use these signatures for creating objects, not your own ideas.\n"
        for (interestingPsiClass: PsiClass in interestingPsiClasses) {
            val interestingPsiClassQN = interestingPsiClass.qualifiedName
            if (interestingPsiClassQN.equals(cut.qualifiedName)) {
                continue
            }

            prompt += "=== methods in class ${interestingPsiClass.qualifiedName}:\n"
            for (currentPsiMethod in interestingPsiClass.methods) {
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

        return prompt;
    }

    fun runTestGeneration() {
        // Send request to LLM
        val originalResponse = llmRequest.request(prompt)

        // Check if response is not empty
        if (originalResponse.isBlank()){
            LLMErrorManager.displayEmptyTests(project)
            return
        }

        TODO("Parse generated tests + Run and validate tests + collect execution results")
    }
}