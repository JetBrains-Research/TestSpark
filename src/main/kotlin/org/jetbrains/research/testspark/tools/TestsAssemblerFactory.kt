package org.jetbrains.research.testspark.tools

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.TestSuiteParser
import org.jetbrains.research.testspark.tools.llm.generation.JUnitTestsAssembler

class TestsAssemblerFactory {
    companion object {
        fun createTestsAssembler(
            indicator: CustomProgressIndicator,
            generationData: TestGenerationData,
            testSuiteParser: TestSuiteParser,
            junitVersion: JUnitVersion,
        ) = JUnitTestsAssembler(indicator, generationData, testSuiteParser, junitVersion)
    }
}
