package org.jetbrains.research.testgenie.actions.evosuite

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.testgenie.actions.updateForClass
import org.jetbrains.research.testgenie.tools.Manager

class GenerateTestsActionClassEvosuite : AnAction() {
    /**
     * Creates and calls (GPT) Pipeline to generate tests for a class when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) = Manager.generateTestsForClassByEvoSuite(e)

    override fun update(e: AnActionEvent) = updateForClass(e, "EvoSuite")
}
