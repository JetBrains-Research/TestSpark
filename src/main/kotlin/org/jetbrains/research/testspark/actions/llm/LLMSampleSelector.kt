package org.jetbrains.research.testspark.actions.llm

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.data.TestSample
import org.jetbrains.research.testspark.testmanager.template.TestAnalyzer

/**
 * A selector for samples for the LLM.
 */
class LLMSampleSelector {
    private val testSamples = mutableListOf<TestSample>()

    private var testSamplesCode: String = ""

    /**
     * Retrieves the test samples code.
     *
     * @return The test samples code.
     */
    fun getTestSamplesCode(): String = testSamplesCode

    /**
     * Provides the list of test names.
     *
     * @return The list of test names.
     */
    fun getTestNames(): MutableList<String> = testSamples.map { it -> it.name }.toMutableList()

    /**
     * Provides the initial test codes.
     *
     * @return The initial test codes
     */
    fun getInitialTestCodes(): MutableList<String> = testSamples.map { it -> it.code }.toMutableList()

    fun appendTestSampleCode(
        index: Int,
        code: String,
    ) {
        testSamplesCode += "Test sample number ${index + 1}\n```\n${code}\n```\n"
    }

    /**
     * Collects the test samples for the LLM from the current project.
     */
    fun collectTestSamples(
        project: Project,
        language: SupportedLanguage,
    ) {
        val testAnalyzer = TestAnalyzer.create(language)

        // add default sample
        testSamples.add(TestSample(DEFAULT_TEST_NAME, DEFAULT_TEST_CODE))

        runReadAction {
            val projectFileIndex: ProjectFileIndex = ProjectRootManager.getInstance(project).fileIndex

            projectFileIndex.iterateContent { file ->
                if (testAnalyzer.isSupportedFileType(file)) {
                    val currentTestSamples = testAnalyzer.getTestSamplesList(project, file)
                    testSamples.addAll(currentTestSamples)
                }

                true
            }
        }
    }

    companion object {
        const val DEFAULT_TEST_NAME = "<html>provide manually</html>"
        const val DEFAULT_TEST_CODE = "// provide test method code here"
    }
}
