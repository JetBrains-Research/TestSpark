package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.mock.MockProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import junit.framework.TestCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestsAssemblerTest : LightJavaCodeInsightFixtureTestCase() {

    private lateinit var indicator: ProgressIndicator
    private lateinit var testsAssembler: TestsAssembler

    private val testClassInit = "public class ExampleTest {"

    private val test1 = "\t@Test\n" +
        "\tpublic void testMultiply() {\n" +
        "\t\tExampleClass exampleClass = new ExampleClass();\n" +
        "\t\tint result = exampleClass.multiply(2, 3);\n" +
        "\t\t\n" +
        "\t\tAssert.assertEquals(6, result);\n" +
        "\t}"

    private val test2 = "\t@Test\n" +
        "\tpublic void testSubtract() {\n" +
        "\t\tExampleClass exampleClass = new ExampleClass();\n" +
        "\t\tint result = exampleClass.subtract(10, 5);\n" +
        "\t\t\n" +
        "\t\tAssert.assertEquals(5, result);\n" +
        "\t}"

    @BeforeEach
    override fun setUp() {
        super.setUp()
        indicator = MockProgressIndicator()
        testsAssembler = TestsAssembler(project, indicator)
    }

    @Test
    fun testEmptyResponse() {
        testsAssembler.receiveResponse("")
        assertTrue(testsAssembler.rawText.isBlank())
        TestCase.assertEquals(0, testsAssembler.lastTestCount)
    }

    @Test
    fun testNonEmptyResponse() {
        testsAssembler.receiveResponse("This is")
        testsAssembler.receiveResponse(" Test")
        TestCase.assertEquals(testsAssembler.rawText, "This is Test")
        TestCase.assertEquals(0, testsAssembler.lastTestCount)
    }

    @Test
    fun testTwoTestsResponse() {
        testsAssembler.receiveResponse(test1)
        testsAssembler.receiveResponse(test2)
        // check number of tests
        TestCase.assertEquals(2, testsAssembler.lastTestCount)
    }

    @Test
    fun testRecieveInCompleteTestClass() {
        testsAssembler.receiveResponse(test1)
        testsAssembler.receiveResponse(test2)

        // check tests
        val generatedTest = testsAssembler.returnTestSuite("org.example")
        TestCase.assertNull(generatedTest)
    }

    @Test
    fun testRecieveFullTestClass() {
        testsAssembler.receiveResponse(testClassInit)
        testsAssembler.receiveResponse(test1)
        testsAssembler.receiveResponse(test2)
        testsAssembler.receiveResponse("}")

        // check tests
        val generatedTest = testsAssembler.returnTestSuite("org.example")
        TestCase.assertEquals(2, generatedTest!!.testCases.size)
        TestCase.assertEquals(test1, generatedTest.testCases[0].toString())
        TestCase.assertEquals(test2, generatedTest.testCases[1].toString())
    }
}
