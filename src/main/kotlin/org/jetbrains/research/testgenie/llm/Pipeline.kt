package org.jetbrains.research.testgenie.llm

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.jetbrains.research.testgenie.actions.getSignatureString

//project, interestingPsiClasses, classFQN, fileUrl, modificationStamp
class Pipeline(
    private val project: Project,
    private val interestingPsiClasses: Set<PsiClass>,
    private val cut: PsiClass,
    private val polymorphismRelations: Map<PsiClass,PsiClass>,
    private val modTs: Long,
) {


    fun forClass() {
        val prompt = generatePrompt()
    }

    private fun generatePrompt(): String {
        var prompt = "Generate unit tests in Java for class ${cut.qualifiedName} to achieve 100% line coverage for this class.\nDont use @Before and @After test methods.\n"


        prompt += "The source code of class under test is as follows:\n ${cut.text}\n"

        prompt += "Here are the method signatures of classes used by the class under test. Only use these signatures for creating objects, not your own ideas.\n"

        for (interestingPsiClass: PsiClass in interestingPsiClasses){
            val interestingPsiClassQN = interestingPsiClass.qualifiedName
            if(interestingPsiClassQN.equals(cut.qualifiedName)){
                continue
            }

            prompt += "=== methods in class ${interestingPsiClass.qualifiedName}:\n"
            for (currentPsiMethod in interestingPsiClass.methods){
                prompt += " - ${currentPsiMethod.getSignatureString()}\n"
            }
            prompt += "\n\n"

            prompt += "=== polymorphism relations:\n"

        }

        return prompt;
    }

    fun runTestGeneration() {
        TODO("Not yet implemented")
    }
}