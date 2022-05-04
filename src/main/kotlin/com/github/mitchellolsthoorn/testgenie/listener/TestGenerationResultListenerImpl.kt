package com.github.mitchellolsthoorn.testgenie.listener

import com.github.mitchellolsthoorn.testgenie.evosuite.TestGenerationResultListener
import com.github.mitchellolsthoorn.testgenie.services.TestCaseDisplayService
import com.github.mitchellolsthoorn.testgenie.toolwindow.TestGenieToolWindowFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.evosuite.utils.CompactReport

class TestGenerationResultListenerImpl(val project: Project) : TestGenerationResultListener {
    private val log = Logger.getInstance(this.javaClass)

    override fun testGenerationResult(testReport: CompactReport) {
        log.info("Received test result for " + testReport.UUT)

        // val actionManager: ActionManager = ActionManager.getInstance()
        // placeholder for firing an action
        val testCaseDisplayService = project.service<TestCaseDisplayService>()

        val tests = testReport.testCaseList.values.map { it.testCode }
        // TODO: display the actual generated test cases
        ApplicationManager.getApplication().invokeLater {
            testCaseDisplayService.displayTestCases(tests)
        }
    }
}