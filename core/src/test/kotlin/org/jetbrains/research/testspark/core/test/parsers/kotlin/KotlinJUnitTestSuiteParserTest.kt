package org.jetbrains.research.testspark.core.test.parsers.kotlin

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.core.test.kotlin.KotlinJUnitTestSuiteParser
import org.jetbrains.research.testspark.core.test.kotlin.KotlinTestBodyPrinter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KotlinJUnitTestSuiteParserTest {

    @Test
    fun testParseTestSuite() {
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

        val testBodyPrinter = KotlinTestBodyPrinter()
        val parser =
            KotlinJUnitTestSuiteParser("org.example", JUnitVersion.JUnit5, testBodyPrinter)
        val testSuite: TestSuiteGeneratedByLLM? = parser.parseTestSuite(text)
        assertNotNull(testSuite)
        assertTrue(testSuite!!.imports.contains("import org.mockito.Mockito.*"))
        assertTrue(testSuite.imports.contains("import org.test.Message as TestMessage"))
        assertTrue(testSuite.imports.contains("import org.mockito.kotlin.mock"))

        val expectedTestCasesNames = listOf(
            "compileTestCases_AllCompilableTest",
            "compileTestCases_NoneCompilableTest",
            "compileTestCases_SomeCompilableTest",
            "compileTestCases_EmptyTestCasesTest",
            "compileTestCases_omg",
        )

        testSuite.testCases.forEachIndexed { index, testCase ->
            val expected = expectedTestCasesNames[index]
            assertEquals(expected, testCase.name) { "${index + 1}st test case has incorrect name" }
        }

        assertTrue(testSuite.testCases[4].expectedException.isNotBlank())
    }

    @Test
    fun testParseEmptyTestSuite() {
        val text = """
            ```kotlin
            package com.example.testsuite
            
            class EmptyTestClass {
            }
            ```
        """.trimIndent()

        val testBodyPrinter = KotlinTestBodyPrinter()
        val parser =
            KotlinJUnitTestSuiteParser("", JUnitVersion.JUnit5, testBodyPrinter)
        val testSuite: TestSuiteGeneratedByLLM? = parser.parseTestSuite(text)
        assertNotNull(testSuite)
        assertEquals(testSuite!!.packageName, "com.example.testsuite")
        assertTrue(testSuite.testCases.isEmpty())
    }

    @Test
    fun testParseSingleTestCase() {
        val text = """
            ```kotlin
            import org.junit.jupiter.api.Test
            
            class SingleTestCaseClass {
                @Test
                fun singleTestCase() {
                    // Test case implementation
                }
            }
            ```
        """.trimIndent()

        val testBodyPrinter = KotlinTestBodyPrinter()
        val parser =
            KotlinJUnitTestSuiteParser("org.example", JUnitVersion.JUnit5, testBodyPrinter)
        val testSuite: TestSuiteGeneratedByLLM? = parser.parseTestSuite(text)
        assertNotNull(testSuite)
        assertEquals(1, testSuite!!.testCases.size)
        assertEquals("singleTestCase", testSuite.testCases[0].name)
    }

    @Test
    fun testParseTwoTestCases() {
        val text = """
            ```kotlin
            import org.junit.jupiter.api.Test
            
            class TwoTestCasesClass {
                @Test
                fun firstTestCase() {
                    // Test case implementation
                }
                
                @Test
                fun secondTestCase() {
                    // Test case implementation
                }
            }
            ```
        """.trimIndent()

        val testBodyPrinter = KotlinTestBodyPrinter()
        val parser =
            KotlinJUnitTestSuiteParser("org.example", JUnitVersion.JUnit5, testBodyPrinter)
        val testSuite: TestSuiteGeneratedByLLM? = parser.parseTestSuite(text)
        assertNotNull(testSuite)
        assertEquals(2, testSuite!!.testCases.size)
        assertEquals("firstTestCase", testSuite.testCases[0].name)
        assertEquals("secondTestCase", testSuite.testCases[1].name)
    }

    @Test
    fun testParseTwoTestCasesWithDifferentPackage() {
        val code1 = """
        ```kotlin
        package org.pkg1
        
        import org.junit.jupiter.api.Test
        
        class TestCasesClass1 {
            @Test
            fun firstTestCase() {
                // Test case implementation
            }
        }  
        ```
        """.trimIndent()

        val code2 = """
        ```kotlin
        package org.pkg2
        
        import org.junit.jupiter.api.Test
        
        class 2TestCasesClass {
            @Test
            fun firstTestCase() {
                // Test case implementation
            }
        }
        ```
        """.trimIndent()

        val testBodyPrinter = KotlinTestBodyPrinter()
        val parser = KotlinJUnitTestSuiteParser("", JUnitVersion.JUnit5, testBodyPrinter)

        // packageName will be set to 'org.pkg1'
        val testSuite1 = parser.parseTestSuite(code1)

        val testSuite2 = parser.parseTestSuite(code2)

        assertNotNull(testSuite1)
        assertNotNull(testSuite2)
        assertEquals("org.pkg1", testSuite1!!.packageName)
        assertEquals("org.pkg2", testSuite2!!.packageName)
    }
}
