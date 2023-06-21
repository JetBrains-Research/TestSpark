package org.jetbrains.research.testgenie.actions.llm

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.testgenie.actions.updateForLine
import org.jetbrains.research.testgenie.tools.Manager

class GenerateTestsActionMethodLlm : AnAction() {
    /**
     * Creates and calls (GPT) Pipeline to generate tests for a method when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) = Manager.generateTestsForMethodByLlm(e)

    override fun update(e: AnActionEvent) = updateForLine(e, "GPT-4")
}
