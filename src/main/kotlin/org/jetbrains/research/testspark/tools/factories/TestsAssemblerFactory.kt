package org.jetbrains.research.testspark.tools.factories

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.TestSuiteParser
import org.jetbrains.research.testspark.tools.llm.generation.JUnitTestsAssembler

object TestsAssemblerFactory {
    fun create(
        indicator: CustomProgressIndicator,
        generationData: TestGenerationData,
        testSuiteParser: TestSuiteParser,
        junitVersion: JUnitVersion,
    ) = JUnitTestsAssembler(indicator, generationData, testSuiteParser, junitVersion)
}
