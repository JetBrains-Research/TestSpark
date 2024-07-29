package org.jetbrains.research.testspark.core.generation.llm.prompt

import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.ClassRepresentation
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.MethodRepresentation
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.PromptGenerationContext
import org.jetbrains.research.testspark.core.generation.llm.prompt.configuration.PromptTemplates

class PromptGenerator(
    private val context: PromptGenerationContext,
    private val promptTemplates: PromptTemplates,
) {
    /**
     * Generates a prompt for a given class under test. It accepts the list of interesting classes and inserts their method signatures.
     *
     * @param interestingClasses The list of ClassRepresentation objects.
     * @return The generated prompt.
     * @throws IllegalStateException If any of the required keywords are missing in the prompt template.
     */
    fun generatePromptForClass(interestingClasses: List<ClassRepresentation>, testSamplesCode: String): String {
        val prompt = PromptBuilder(promptTemplates.classPrompt)
            .insertLanguage(context.promptConfiguration.desiredLanguage)
            .insertName(context.cut!!.qualifiedName)
            .insertTestingPlatform(context.promptConfiguration.desiredTestingPlatform)
            .insertMockingFramework(context.promptConfiguration.desiredMockingFramework)
            .insertCodeUnderTest(context.cut.fullText, context.classesToTest)
            .insertMethodsSignatures(interestingClasses)
            .insertPolymorphismRelations(context.polymorphismRelations)
            .insertTestSample(testSamplesCode)
            .build()

        println("Prompt: $prompt")
        return prompt
    }

    /**
     * Generates a prompt for a given method representation and a list of interesting classes.
     *
     * @param method The representation of a method to be tested.
     * @param interestingClassesFromMethod A list of interesting classes related to the method.
     * @return The generated prompt.
     * @throws IllegalStateException If any of the required keywords are missing in the prompt template.
     */
    fun generatePromptForMethod(
        method: MethodRepresentation,
        interestingClassesFromMethod: List<ClassRepresentation>,
        testSamplesCode: String,
        packageName: String,
    ): String {
        val name = context.cut?.let { "${it.qualifiedName}.${method.name}" } ?: "$packageName.${method.name}"
        val prompt = PromptBuilder(promptTemplates.methodPrompt)
            .insertLanguage(context.promptConfiguration.desiredLanguage)
            .insertName(name)
            .insertTestingPlatform(context.promptConfiguration.desiredTestingPlatform)
            .insertMockingFramework(context.promptConfiguration.desiredMockingFramework)
            .insertCodeUnderTest(method.text, context.classesToTest)
            .insertMethodsSignatures(interestingClassesFromMethod)
            .insertPolymorphismRelations(context.polymorphismRelations)
            .insertTestSample(testSamplesCode)
            .build()

        return prompt
    }

    /**
     * Generates a prompt for a given line under test.
     * It accepts the code of a line under test, a representation of the method that contains the line, and the set of interesting classes (e.g., the containing class of the method, classes listed in parameters of the method and constructors of the containing class).
     *
     * @param lineUnderTest The source code of the line to be tested.
     * @param method The representation of the method that contains the line.
     * @param interestingClassesFromMethod A list of interesting classes related to the method.
     * @return The generated prompt.
     * @throws IllegalStateException If any of the required keywords are missing in the prompt template.
     */
    fun generatePromptForLine(
        lineUnderTest: String,
        method: MethodRepresentation,
        interestingClassesFromMethod: List<ClassRepresentation>,
        testSamplesCode: String,
    ): String {
        val prompt = PromptBuilder(promptTemplates.linePrompt)
            .insertLanguage(context.promptConfiguration.desiredLanguage)
            .insertName(lineUnderTest.trim())
            .insertTestingPlatform(context.promptConfiguration.desiredTestingPlatform)
            .insertMockingFramework(context.promptConfiguration.desiredMockingFramework)
            .insertCodeUnderTest(method.text, context.classesToTest)
            .insertMethodsSignatures(interestingClassesFromMethod)
            .insertPolymorphismRelations(context.polymorphismRelations)
            .insertTestSample(testSamplesCode)
            .build()

        return prompt
    }
}
