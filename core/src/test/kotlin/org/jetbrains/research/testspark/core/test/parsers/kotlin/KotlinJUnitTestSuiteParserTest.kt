package org.jetbrains.research.testspark.core.test.parsers.kotlin

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.core.test.kotlin.KotlinJUnitTestSuiteParser
import org.jetbrains.research.testspark.core.utils.kotlinImportPattern
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class KotlinJUnitTestSuiteParserTest {

    @Test
    fun testFunction() {
        val text = """
            ```kotlin
            import org.junit.jupiter.api.Assertions.*
            import org.junit.jupiter.api.Test
            import org.mockito.Mockito.*
            import org.mockito.kotlin.any
            import org.mockito.kotlin.eq
            import org.mockito.kotlin.mock
            import org.test.Message as TestMessage
            
            class MyClassTest {
            
                @Test
                fun compileTestCases_AllCompilableTest() {
                    // Arrange
                    val myClass = MyClass()
                    val generatedTestCasesPaths = listOf("path1", "path2")
                    val buildPath = "buildPath"
                    val testCase1 = TestCaseGeneratedByLLM()
                    val testCase2 = TestCaseGeneratedByLLM()
                    val testCases = mutableListOf(testCase1, testCase2)
            
                    val myClassSpy = spy(myClass)
                    doReturn(Pair(true, "")).`when`(myClassSpy).compileCode(any(), eq(buildPath))
            
                    // Act
                    val result = myClassSpy.compileTestCases(generatedTestCasesPaths, buildPath, testCases)
            
                    // Assert
                    assertTrue(result.allTestCasesCompilable)
                    assertEquals(setOf(testCase1, testCase2), result.compilableTestCases)
                }
            
                @Test
                fun compileTestCases_NoneCompilableTest() {
                    // Arrange
                    val myClass = MyClass()
                    val generatedTestCasesPaths = listOf("path1", "path2")
                    val buildPath = "buildPath"
                    val testCase1 = TestCaseGeneratedByLLM()
                    val testCase2 = TestCaseGeneratedByLLM()
                    val testCases = mutableListOf(testCase1, testCase2)
            
                    val myClassSpy = spy(myClass)
                    doReturn(Pair(false, "")).`when`(myClassSpy).compileCode(any(), eq(buildPath))
            
                    // Act
                    val result = myClassSpy.compileTestCases(generatedTestCasesPaths, buildPath, testCases)
            
                    // Assert
                    assertFalse(result.allTestCasesCompilable)
                    assertTrue(result.compilableTestCases.isEmpty())
                }
            
                @Test
                fun compileTestCases_SomeCompilableTest() {
                    // Arrange
                    val myClass = MyClass()
                    val generatedTestCasesPaths = listOf("path1", "path2")
                    val buildPath = "buildPath"
                    val testCase1 = TestCaseGeneratedByLLM()
                    val testCase2 = TestCaseGeneratedByLLM()
                    val testCases = mutableListOf(testCase1, testCase2)
            
                    val myClassSpy = spy(myClass)
                    doReturn(Pair(true, "")).`when`(myClassSpy).compileCode(eq("path1"), eq(buildPath))
                    doReturn(Pair(false, "")).`when`(myClassSpy).compileCode(eq("path2"), eq(buildPath))
            
                    // Act
                    val result = myClassSpy.compileTestCases(generatedTestCasesPaths, buildPath, testCases)
            
                    // Assert
                    assertFalse(result.allTestCasesCompilable)
                    assertEquals(setOf(testCase1), result.compilableTestCases)
                }
            
                @Test
                fun compileTestCases_EmptyTestCasesTest() {
                    // Arrange
                    val myClass = MyClass()
                    val generatedTestCasesPaths = emptyList<String>()
                    val buildPath = "buildPath"
                    val testCases = mutableListOf<TestCaseGeneratedByLLM>()
            
                    // Act
                    val result = myClass.compileTestCases(generatedTestCasesPaths, buildPath, testCases)
            
                    // Assert
                    assertTrue(result.allTestCasesCompilable)
                    assertTrue(result.compilableTestCases.isEmpty())
                }
                
                @Test(expected = ArithmeticException::class, Exception::class)
                fun compileTestCases_omg() {
                    val blackHole = 1 / 0
                }
            }
            ```
        """.trimIndent()
        val parser = KotlinJUnitTestSuiteParser("org.my.package", JUnitVersion.JUnit5, kotlinImportPattern)
        val testSuite: TestSuiteGeneratedByLLM? = parser.parseTestSuite(text)
        assertNotNull(testSuite)
        assert(testSuite.imports.contains("import org.mockito.Mockito.*"))
        assert(testSuite.imports.contains("import org.test.Message as TestMessage"))
        assert(testSuite.imports.contains("import org.mockito.kotlin.mock"))
        assert(testSuite.testCases[0].name == "compileTestCases_AllCompilableTest")
        assert(testSuite.testCases[1].name == "compileTestCases_NoneCompilableTest")
        assert(testSuite.testCases[2].name == "compileTestCases_SomeCompilableTest")
        assert(testSuite.testCases[3].name == "compileTestCases_EmptyTestCasesTest")
        assert(testSuite.testCases[4].name == "compileTestCases_omg")
        assert(testSuite.testCases[4].expectedException.isNotBlank())
    }
}
