package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.test.TestSuiteGeneratedByLLM

abstract class RequestManager {
    open val token: String = SettingsArguments.llmUserToken()
    open val chatHistory = mutableListOf<ChatMessage>()

    open val log: Logger = Logger.getInstance(this.javaClass)

    abstract fun request(
        prompt: String,
        indicator: ProgressIndicator,
        packageName: String,
        project: Project,
        llmErrorManager: LLMErrorManager,
        isUserFeedback: Boolean = false
    ): Pair<String, TestSuiteGeneratedByLLM?>

    open fun processResponse(
        testsAssembler: TestsAssembler,
        packageName: String,
    ): Pair<String, TestSuiteGeneratedByLLM?> {

        if(testsAssembler.rawText.isEmpty()){
            return Pair("", null)
        }
        // save the full response in the chat history
        val response = testsAssembler.rawText
        log.info("The full response: \n $response")
        chatHistory.add(ChatMessage("assistant", response))

        // check if response is empty
        if (response.isEmpty() || response.isBlank()) {
            return Pair(
                "You have provided an empty answer! Please answer my previous question with the same formats",
                null,
            )
        }

        val testSuiteGeneratedByLLM = testsAssembler.returnTestSuite(packageName)
            ?: return Pair("The provided code is not parsable. Please give the correct code", null)

        return Pair("", testSuiteGeneratedByLLM.reformat())
    }

    abstract fun send(prompt: String,
                      indicator: ProgressIndicator,
                      project: Project,
                      llmErrorManager: LLMErrorManager): TestsAssembler

    open fun processUserFeedbackResponse(testsAssembler: TestsAssembler,
                                         packageName: String) : Pair<String, TestSuiteGeneratedByLLM?>{
        val response = testsAssembler.rawText
        log.info("The full response: \n $response")

        val testSuiteGeneratedByLLM = testsAssembler.returnTestSuite(packageName)

        return Pair("", testSuiteGeneratedByLLM)
    }
}
