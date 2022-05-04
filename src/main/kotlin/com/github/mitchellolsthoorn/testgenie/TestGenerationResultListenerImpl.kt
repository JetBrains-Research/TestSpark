package com.github.mitchellolsthoorn.testgenie

import com.github.mitchellolsthoorn.testgenie.evo.TestGenerationResultListener
import com.github.mitchellolsthoorn.testgenie.toolwindow.TestGenieToolWindowFactory
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import org.evosuite.utils.CompactReport

class TestGenerationResultListenerImpl(val project: Project) : TestGenerationResultListener {
    private val log = Logger.getInstance(this.javaClass)

    override fun testGenerationResult(test: CompactReport) {
        log.info("Received test result for " + test.UUT)

        // val actionManager: ActionManager = ActionManager.getInstance()
        // placeholder for firing an action

    }
}