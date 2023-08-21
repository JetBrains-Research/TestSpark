package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.test.TestSuiteGeneratedByLLM

class GrazieRequestManager : RequestManager() {

    override fun request(
        prompt: String,
        indicator: ProgressIndicator,
        packageName: String,
        project: Project,
        llmErrorManager: LLMErrorManager
    ): Pair<String, TestSuiteGeneratedByLLM?> {

        // update chat history
        chatHistory.add(ChatMessage("user", prompt))

        val messages = getMessages()

        return try {
            val className = "org.jetbrains.research.grazie.Request"
            val request: Request = Class.forName(className).getDeclaredConstructor().newInstance() as Request
            val testsAssembler = request.request(token, messages, TestsAssembler(project, indicator))

            return processResponse(testsAssembler, packageName)
        } catch (e: ClassNotFoundException) {
            llmErrorManager.errorProcess("Grazie test generation feature is not available in this build.", project)
            Pair("", null)
        }
    }

    private fun getMessages(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        chatHistory.forEach {
            result.add(Pair(it.role, it.content))
        }
        return result
    }
}
