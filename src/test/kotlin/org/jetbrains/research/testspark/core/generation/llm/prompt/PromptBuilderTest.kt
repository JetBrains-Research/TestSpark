package org.jetbrains.research.testspark.core.generation.llm.prompt

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains

class PromptBuilderTest {
    @BeforeEach
    fun setUp() {

    }

    @Test
    fun insertLanguage() {
        val (keyword, value) = PromptKeyword.LANGUAGE to "Java"

        val prompt = PromptBuilder("Language: ${keyword.variable}")
            .insertLanguage(value)
            .build()

        assertEquals("Language: Java", prompt)
    }

    @Test
    fun insertName() {
        val (keyword, value) = PromptKeyword.NAME to "MyClass"

        val prompt = PromptBuilder("Name: ${keyword.variable}")
            .insertName(value)
            .build()

        assertEquals("Name: MyClass", prompt)
    }

    @Test
    fun insertTestingPlatform() {
        val (keyword, value) = PromptKeyword.TESTING_PLATFORM to "JUnit4"

        val prompt = PromptBuilder("Testing platform: ${keyword.variable}")
            .insertTestingPlatform(value)
            .build()

        assertEquals("Testing platform: JUnit4", prompt)
    }

    @Test
    fun insertMockingFramework() {
        val (keyword, value) = PromptKeyword.MOCKING_FRAMEWORK to "Mockito"

        val prompt = PromptBuilder("Mocking framework: ${keyword.variable}")
            .insertMockingFramework(value)
            .build()

        assertEquals("Mocking framework: Mockito", prompt)
    }

    @Test
    fun insertCodeUnderTest() {
        val keyword = PromptKeyword.CODE
        val code = """
            class MyClass() {
              fun f() { println("Hello world!") }
            }
        """.trimIndent()

        val prompt = PromptBuilder("Code:\n${keyword.variable}")
            .insertCodeUnderTest(code, emptyList())
            .build()

        assertContains(prompt, code, message = "Code under test should be inserted into prompt template")
    }

    @Test
    fun lastInsertionPrevails() {
        val keyword = PromptKeyword.LANGUAGE

        val prompt = PromptBuilder("Language: ${keyword.variable}")
            .insertLanguage("Java")
            .insertLanguage("Kotlin")
            .build()

        assertEquals("Language: Kotlin", prompt)
    }

    @Test
    fun throwsOnMissingMandatoryKeyword() {
        val keywords: List<PromptKeyword> = PromptKeyword.entries

        for (keyword in keywords) {
            val promptTemplate = "My variable is: ${keyword.variable}"

            if (keyword.mandatory) {
                val exception = assertThrows<IllegalStateException> { PromptBuilder(promptTemplate).build() }
                assertEquals("The prompt must contain ${keyword.text}", exception.message)
            }
            else {
                assertDoesNotThrow { PromptBuilder(promptTemplate).build() }
            }
        }
    }

    @Test
    fun testPopulateMultipleVariableEntries() {
        val keyword = PromptKeyword.LANGUAGE
        val template = """
            Language1: '${keyword.variable}'
            Language2: \\${keyword.variable}\\
            Language3: `${keyword.variable}`
        """.trimIndent()

        val prompt = PromptBuilder(template)
            .insertLanguage("Java")
            .build()

        assertEquals("""
            Language1: 'Java'
            Language2: \\Java\\
            Language3: `Java`
        """.trimIndent(), prompt)
    }

    @Test
    fun insertMultipleVariables() {
        val template = """
            language: ${PromptKeyword.LANGUAGE.variable}
            name: ${PromptKeyword.NAME.variable}
            testing platform: ${PromptKeyword.TESTING_PLATFORM.variable}
            mocking framework: ${PromptKeyword.MOCKING_FRAMEWORK.variable}
        """.trimIndent()

        val prompt = PromptBuilder(template)
            .insertLanguage("Java")
            .insertName("org.pkg.MyClass")
            .insertTestingPlatform("JUnit4")
            .insertMockingFramework("Mockito")
            .build()

        val expected = """
            language: Java
            name: org.pkg.MyClass
            testing platform: JUnit4
            mocking framework: Mockito
        """.trimIndent()

        assertEquals(expected, prompt)
    }

    @Test
    fun insertMethodsSignatures() {
        // TODO: finish
    }

    @Test
    fun insertPolymorphismRelations() {
        // TODO: finish
    }

    @Test
    fun insertTestSample() {
        // TODO: finish
    }
}