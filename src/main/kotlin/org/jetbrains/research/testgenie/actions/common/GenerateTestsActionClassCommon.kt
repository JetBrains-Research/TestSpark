package org.jetbrains.research.testgenie.actions.common

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.testgenie.actions.updateForClass
import org.jetbrains.research.testgenie.tools.Manager

/**
 * This class contains all the logic related to generating tests for a class.
 * No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available,
 *   getting the information about the selected class and passing it to (EvoSuite) Pipeline.
 */
class GenerateTestsActionClassCommon : AnAction() {
    /**
     * Creates and calls (EvoSuite) Pipeline to generate tests for a class when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) = Manager.generateTestsForClass(e)

    override fun update(e: AnActionEvent) = updateForClass(e, "all test generators")
}
