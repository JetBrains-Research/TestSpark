package org.jetbrains.research.testspark.core.generation.llm.prompt

import org.jetbrains.research.testspark.core.data.ClassType
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.ClassRepresentation
import java.util.EnumMap


/**
 * Populates variables within the prompt template with the provided values.
 * Adheres to the **Builder Pattern**.
 */
class PromptBuilder(private val promptTemplate: String) {
    private val insertedKeywordValues: EnumMap<PromptKeyword, String> = EnumMap(PromptKeyword::class.java)
    // collect all the keywords present in the prompt template
    private val templateKeywords: List<PromptKeyword> = buildList {
        for (keyword in PromptKeyword.entries) {
            if (promptTemplate.contains(keyword.variable)) {
                add(keyword)
            }
        }
    }

    /**
     * Populates the `promptTemplate` with the values for keywords present in `insertedKeywordValues`.
     * Validates that all mandatory fields are filled.
     */
    fun build(): String {
        var populatedPrompt = promptTemplate

        // populate the template with the inserted values
        for ((keyword, value) in insertedKeywordValues.entries) {
            populatedPrompt = populatedPrompt.replace(keyword.variable, value, ignoreCase = false)
        }

        // validate that all mandatory keywords were provided
        for (keyword in templateKeywords) {
            if (!insertedKeywordValues.contains(keyword) && keyword.mandatory) {
                throw IllegalStateException("The prompt must contain ${keyword.text} keyword")
            }
        }

        return populatedPrompt
    }

    private fun insert(keyword: PromptKeyword, value: String) {
        if (!templateKeywords.contains(keyword) && keyword.mandatory) {
            throw IllegalArgumentException("Prompt template does not contain mandatory ${keyword.text}")
        }
        insertedKeywordValues[keyword] = value
    }

    fun insertLanguage(language: String) = apply {
        insert(PromptKeyword.LANGUAGE, language)
    }

    fun insertName(classDisplayName: String) = apply {
        insert(PromptKeyword.NAME, classDisplayName)
    }

    fun insertTestingPlatform(testingPlatformName: String) = apply {
        insert(PromptKeyword.TESTING_PLATFORM, testingPlatformName)
    }

    fun insertMockingFramework(mockingFrameworkName: String) = apply {
        insert(PromptKeyword.MOCKING_FRAMEWORK, mockingFrameworkName)
    }

    // TODO: rename variables (not class but code construct)
    fun insertCodeUnderTest(classFullText: String, classesToTest: List<ClassRepresentation>) = apply {
        var fullText = "```\n${classFullText}\n```\n"
        for (i in 2..classesToTest.size) {
            val subClass = classesToTest[i - 2]
            val superClass = classesToTest[i - 1]

            fullText += "${subClass.qualifiedName} extends ${superClass.qualifiedName}. " +
                    "The source code of ${superClass.qualifiedName} is:\n```\n${superClass.fullText}\n" +
                    "```\n"
        }

        insert(PromptKeyword.CODE, fullText)
    }

    fun insertMethodsSignatures(interestingClasses: List<ClassRepresentation>) = apply {
        var fullText = ""
        if (interestingClasses.isNotEmpty()) {
            fullText += "Here are some information about other methods and classes used by the class under test. Only use them for creating objects, not your own ideas.\n"
        }

        for (interestingClass in interestingClasses) {
            if (interestingClass.qualifiedName.startsWith("java")  || interestingClass.qualifiedName.startsWith("kotlin")) {
                continue
            }

            fullText += "=== methods in ${interestingClass.qualifiedName}:\n"

            for (method in interestingClass.allMethods) {
                // Skip java methods
                // TODO: checks for java methods should be done by a caller to make
                //       this class as abstract and language agnostic as possible.
                if (method.containingClassQualifiedName.startsWith("java") ||
                    method.containingClassQualifiedName.startsWith("kotlin")) {
                    continue
                }

                fullText += " - ${method.signature}\n"
            }
        }

        insert(PromptKeyword.METHODS, fullText)
    }

    fun insertPolymorphismRelations(
        polymorphismRelations: Map<ClassRepresentation, List<ClassRepresentation>>,
    ) = apply {
        var fullText = when {
            polymorphismRelations.isNotEmpty() -> "Use the following polymorphic relationships of classes present in the project. Use them for instantiation when necessary. Do not mock classes if an instantiation of a sub-class is applicable"
            else -> ""
        }

        for (entry in polymorphismRelations) {
            for (currentSubClass in entry.value) {
                val subClassTypeName = when (currentSubClass.classType) {
                    ClassType.INTERFACE -> "an interface implementing"
                    ClassType.ABSTRACT_CLASS -> "an abstract sub-class of"
                    ClassType.CLASS -> "a sub-class of"
                    ClassType.DATA_CLASS -> "a sub data class of"
                    ClassType.INLINE_VALUE_CLASS -> "a sub inline value class class of"
                    ClassType.OBJECT -> "a sub object of"
                }
                fullText += "${currentSubClass.qualifiedName} is $subClassTypeName ${entry.key.qualifiedName}.\n"
            }
        }

        insert(PromptKeyword.POLYMORPHISM, fullText)
    }

    fun insertTestSample(testSamplesCode: String) = apply {
        var fullText = testSamplesCode
        if (fullText.isNotBlank()) {
            fullText = "Use this test samples:\n$fullText\n"
        }

        insert(PromptKeyword.TEST_SAMPLE, fullText)
    }
}
