package org.jetbrains.research.testgenie.actions.evosuite

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.testgenie.actions.updateForMethod
import org.jetbrains.research.testgenie.tools.Manager

/**
 * This class contains all the logic related to generating tests for a class by EvoSuite.
 * No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available,
 *   getting the information about the selected line and passing it to Pipeline.
 */
class GenerateTestsActionMethodEvosuite : AnAction() {
    /**
     * Creates and calls Pipeline to generate tests for a class when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) = Manager.generateTestsForMethodByEvoSuite(e)

    /**
     * Makes the action visible only if a class has been selected.
     * It also updates the action name depending on which class has been selected.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun update(e: AnActionEvent) = updateForMethod(e, "EvoSuite")
}
