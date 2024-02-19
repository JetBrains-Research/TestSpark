package org.jetbrains.research.testspark.core.generation.prompt

import org.jetbrains.research.testspark.core.generation.prompt.configuration.ClassRepresentation
import org.jetbrains.research.testspark.core.generation.prompt.configuration.MethodRepresentation
import org.jetbrains.research.testspark.core.generation.prompt.configuration.PromptGenerationContext
import org.jetbrains.research.testspark.core.generation.prompt.configuration.PromptTemplates

class PromptGenerator(
    private val context: PromptGenerationContext,
    private val promptTemplates: PromptTemplates,
) {
    fun generatePromptForClass(interestingClasses: List<ClassRepresentation>): String {
        var classPrompt = promptTemplates.classPrompt

        classPrompt = insertLanguage(classPrompt)
        classPrompt = insertName(classPrompt, context.cut.qualifiedName)
        classPrompt = insertTestingPlatform(classPrompt)
        classPrompt = insertMockingFramework(classPrompt)
        classPrompt = insertCodeUnderTest(classPrompt, context.cut.fullText)
        classPrompt = insertMethodsSignatures(classPrompt, interestingClasses)
        classPrompt = insertPolymorphismRelations(classPrompt, context.polymorphismRelations)

        return classPrompt
    }

    fun generatePromptForMethod(
        method: MethodRepresentation,
        interestingClassesFromMethod: List<ClassRepresentation>,
    ): String {
        var methodPrompt = promptTemplates.methodPrompt

        methodPrompt = insertLanguage(methodPrompt)
        methodPrompt = insertName(methodPrompt, "${context.cut.qualifiedName}.${method.name}")
        methodPrompt = insertTestingPlatform(methodPrompt)
        methodPrompt = insertMockingFramework(methodPrompt)
        methodPrompt = insertCodeUnderTest(methodPrompt, method.text)
        methodPrompt = insertMethodsSignatures(methodPrompt, interestingClassesFromMethod)
        methodPrompt = insertPolymorphismRelations(methodPrompt, context.polymorphismRelations)

        return methodPrompt
    }

    fun generatePromptForLine(
        lineUnderTest: String,
        method: MethodRepresentation,
        interestingClassesFromMethod: List<ClassRepresentation>,
    ): String {
        var linePrompt = promptTemplates.linePrompt

        linePrompt = insertLanguage(linePrompt)
        linePrompt = insertName(linePrompt, lineUnderTest.trim())
        linePrompt = insertTestingPlatform(linePrompt)
        linePrompt = insertMockingFramework(linePrompt)
        linePrompt = insertCodeUnderTest(linePrompt, method.text)
        linePrompt = insertMethodsSignatures(linePrompt, interestingClassesFromMethod)
        linePrompt = insertPolymorphismRelations(linePrompt, context.polymorphismRelations)

        return linePrompt
    }

    // TODO: move the below methods into a PromptBuilder class
    private fun isPromptValid(
        keyword: PromptKeyword,
        prompt: String,
    ): Boolean {
        val keywordText = keyword.text
        val isMandatory = keyword.mandatory

        return (prompt.contains(keywordText) || !isMandatory)
    }

    private fun insertLanguage(classPrompt: String): String {
        if (isPromptValid(PromptKeyword.LANGUAGE, classPrompt)) {
            val keyword = "\$${PromptKeyword.LANGUAGE.text}"
            return classPrompt.replace(keyword, "Java", ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.LANGUAGE.text}")
        }
    }

    private fun insertName(
        classPrompt: String,
        classDisplayName: String,
    ): String {
        if (isPromptValid(PromptKeyword.NAME, classPrompt)) {
            val keyword = "\$${PromptKeyword.NAME.text}"
            return classPrompt.replace(keyword, classDisplayName, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.NAME.text}")
        }
    }

    private fun insertTestingPlatform(classPrompt: String): String {
        if (isPromptValid(PromptKeyword.TESTING_PLATFORM, classPrompt)) {
            val keyword = "\$${PromptKeyword.TESTING_PLATFORM.text}"
            return classPrompt.replace(keyword, "JUnit 4", ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.TESTING_PLATFORM.text}")
        }
    }

    private fun insertMockingFramework(classPrompt: String): String {
        if (isPromptValid(PromptKeyword.MOCKING_FRAMEWORK, classPrompt)) {
            val keyword = "\$${PromptKeyword.MOCKING_FRAMEWORK.text}"
            return classPrompt.replace(keyword, "Mockito 5", ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.MOCKING_FRAMEWORK.text}")
        }
    }

    private fun insertCodeUnderTest(
        classPrompt: String,
        classFullText: String,
    ): String {
        if (isPromptValid(PromptKeyword.CODE, classPrompt)) {
            val keyword = "\$${PromptKeyword.CODE.text}"
            var fullText = "```\n${classFullText}\n```\n"

            for (i in 2..context.classesToTest.size) {
                val subClass = context.classesToTest[i - 2]
                val superClass = context.classesToTest[i - 1]

                fullText += "${subClass.qualifiedName} extends ${superClass.qualifiedName}. " +
                    "The source code of ${superClass.qualifiedName} is:\n```\n${superClass.fullText}\n" +
                    "```\n"
            }
            return classPrompt.replace(keyword, fullText, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.CODE.text}")
        }
    }

    private fun insertMethodsSignatures(
        classPrompt: String,
        interestingClasses: List<ClassRepresentation>,
    ): String {
        val keyword = "\$${PromptKeyword.METHODS.text}"

        if (isPromptValid(PromptKeyword.METHODS, classPrompt)) {
            var fullText = ""
            for (interestingClass in interestingClasses) {
                fullText += "=== methods in ${interestingClass.qualifiedName}:\n"

                for (method in interestingClass.methods) {
                    fullText += " - ${method.signature}\n"
                }
            }
            return classPrompt.replace(keyword, fullText, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.METHODS.text}")
        }
    }

    private fun insertPolymorphismRelations(
        classPrompt: String,
        polymorphismRelations: Map<ClassRepresentation, List<ClassRepresentation>>,
    ): String {
        val keyword = "\$${PromptKeyword.POLYMORPHISM.text}"
        if (isPromptValid(PromptKeyword.POLYMORPHISM, classPrompt)) {
            var fullText = ""

            polymorphismRelations.forEach { entry ->
                for (currentSubClass in entry.value) {
                    fullText += "${currentSubClass.qualifiedName} is a sub-class of ${entry.key.qualifiedName}.\n"
                }
            }
            return classPrompt.replace(keyword, fullText, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.POLYMORPHISM.text}")
        }
    }
}
