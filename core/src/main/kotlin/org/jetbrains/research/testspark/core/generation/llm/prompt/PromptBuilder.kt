package org.jetbrains.research.testspark.core.generation.llm.prompt

import org.jetbrains.research.testspark.core.data.ClassType
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.ClassRepresentation
import java.util.EnumMap


/**
 * Provides variables from the underlying keyword.
 */
private val PromptKeyword.variable: String
    get() = "\$${this.text}"


/**
 * Populates variables within the prompt template with the provided values.
 * Adheres to the Builder Pattern.
 */
class PromptBuilder(private val promptTemplate: String) {
    private val insertedKeywordValues: EnumMap<PromptKeyword, String> = EnumMap(PromptKeyword::class.java)
    private val templateKeywords: List<PromptKeyword>

    init {
        // collect all the keywords present in the prompt template
        templateKeywords = mutableListOf()

        for (keyword in PromptKeyword.entries) {
            if (containsPromptKeyword(keyword)) {
                templateKeywords.add(keyword)
            }
        }
    }

    private fun containsPromptKeyword(keyword: PromptKeyword): Boolean = promptTemplate.contains(keyword.variable)

    private fun validatePromptKeyword(keyword: PromptKeyword) {
        if (!insertedKeywordValues.contains(keyword) && keyword.mandatory) {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.LANGUAGE.text}")
        }
    }

    fun build(): String {
        var populatedPrompt = promptTemplate

        // populate the template with the inserted values
        for ((keyword, value) in insertedKeywordValues.entries) {
            populatedPrompt = populatedPrompt.replace(keyword.variable, value, ignoreCase = false)
        }

        // validate that all mandatory keywords were provided
        for (keyword in templateKeywords) {
            validatePromptKeyword(keyword)
        }

        return populatedPrompt
    }

    private fun insert(keyword: PromptKeyword, value: String) {
        insertedKeywordValues[keyword] = value
    }

    fun insertLanguage(language: String) = apply {
        insert(PromptKeyword.LANGUAGE, language)
        /*if (requiresPromptKeyword(PromptKeyword.LANGUAGE, promptTemplate)) {
            val keyword = "\$${PromptKeyword.LANGUAGE.text}"
            promptTemplate = promptTemplate.replace(keyword, language, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.LANGUAGE.text}")
        }*/
    }

    fun insertName(classDisplayName: String) = apply {
        insert(PromptKeyword.NAME, classDisplayName)
        /*if (requiresPromptKeyword(PromptKeyword.NAME, promptTemplate)) {
            val keyword = "\$${PromptKeyword.NAME.text}"
            promptTemplate = promptTemplate.replace(keyword, classDisplayName, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.NAME.text}")
        }*/
    }

    fun insertTestingPlatform(testingPlatformName: String) = apply {
        insert(PromptKeyword.TESTING_PLATFORM, testingPlatformName)
        /*if (requiresPromptKeyword(PromptKeyword.TESTING_PLATFORM, promptTemplate)) {
            val keyword = "\$${PromptKeyword.TESTING_PLATFORM.text}"
            promptTemplate = promptTemplate.replace(keyword, testingPlatformName, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.TESTING_PLATFORM.text}")
        }*/
    }

    fun insertMockingFramework(mockingFrameworkName: String) = apply {
        insert(PromptKeyword.MOCKING_FRAMEWORK, mockingFrameworkName)
        /*if (requiresPromptKeyword(PromptKeyword.MOCKING_FRAMEWORK, promptTemplate)) {
            val keyword = "\$${PromptKeyword.MOCKING_FRAMEWORK.text}"
            promptTemplate = promptTemplate.replace(keyword, mockingFrameworkName, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.MOCKING_FRAMEWORK.text}")
        }*/
    }

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
        /*if (requiresPromptKeyword(PromptKeyword.CODE, promptTemplate)) {
            val keyword = "\$${PromptKeyword.CODE.text}"
            var fullText = "```\n${classFullText}\n```\n"

            for (i in 2..classesToTest.size) {
                val subClass = classesToTest[i - 2]
                val superClass = classesToTest[i - 1]

                fullText += "${subClass.qualifiedName} extends ${superClass.qualifiedName}. " +
                    "The source code of ${superClass.qualifiedName} is:\n```\n${superClass.fullText}\n" +
                    "```\n"
            }
            promptTemplate = promptTemplate.replace(keyword, fullText, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.CODE.text}")
        }*/
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
        /*
        val keyword = "\$${PromptKeyword.METHODS.text}"

        if (requiresPromptKeyword(PromptKeyword.METHODS, promptTemplate)) {
            var fullText = ""
            if (interestingClasses.isNotEmpty()) {
                fullText += "Here are some information about other methods and classes used by the class under test. Only use them for creating objects, not your own ideas.\n"
            }
            for (interestingClass in interestingClasses) {
                if (interestingClass.qualifiedName.startsWith("java") || interestingClass.qualifiedName.startsWith("kotlin")) {
                    continue
                }

                fullText += "=== methods in ${interestingClass.qualifiedName}:\n"

                for (method in interestingClass.allMethods) {
                    // Skip java methods
                    // TODO: checks for java methods should be done by a caller to make
                    //       this class as abstract and language agnostic as possible.
                    if (method.containingClassQualifiedName.startsWith("java") ||
                        method.containingClassQualifiedName.startsWith("kotlin")
                    ) {
                        continue
                    }

                    fullText += " - ${method.signature}\n"
                }
            }
            promptTemplate = promptTemplate.replace(keyword, fullText, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.METHODS.text}")
        }*/
    }

    fun insertPolymorphismRelations(
        polymorphismRelations: Map<ClassRepresentation, List<ClassRepresentation>>,
    ) = apply {
        var fullText = when {
            polymorphismRelations.isNotEmpty() -> "Use the following polymorphic relationships of classes present in the project. Use them for instantiation when necessary. Do not mock classes if an instantiation of a sub-class is applicable"
            else -> ""
        }

        polymorphismRelations.forEach { entry ->
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

        /*
        val keyword = "\$${PromptKeyword.POLYMORPHISM.text}"
        if (requiresPromptKeyword(PromptKeyword.POLYMORPHISM, promptTemplate)) {
            var fullText = ""

        if (isPromptValid(PromptKeyword.POLYMORPHISM, prompt)) {
            // If polymorphismRelations is not empty, we add an instruction to avoid mocking classes if an instantiation of a sub-class is applicable
            var fullText = when {
                polymorphismRelations.isNotEmpty() -> "Use the following polymorphic relationships of classes present in the project. Use them for instantiation when necessary. Do not mock classes if an instantiation of a sub-class is applicable"
                else -> ""
            }
            polymorphismRelations.forEach { entry ->
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
            promptTemplate = promptTemplate.replace(keyword, fullText, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.POLYMORPHISM.text}")
        }*/
    }

    fun insertTestSample(testSamplesCode: String) = apply {
        var fullText = testSamplesCode
        if (fullText.isNotBlank()) {
            fullText = "Use this test samples:\n$fullText\n"
        }

        insert(PromptKeyword.TEST_SAMPLE, fullText)

        /*
        val keyword = "\$${PromptKeyword.TEST_SAMPLE.text}"

        if (requiresPromptKeyword(PromptKeyword.TEST_SAMPLE, promptTemplate)) {
            var fullText = testSamplesCode
            if (fullText.isNotBlank()) {
                fullText = "Use this test samples:\n$fullText\n"
            }
            promptTemplate = promptTemplate.replace(keyword, fullText, ignoreCase = false)
        } else {
            throw IllegalStateException("The prompt must contain ${PromptKeyword.TEST_SAMPLE.text}")
        }*/
    }
}
