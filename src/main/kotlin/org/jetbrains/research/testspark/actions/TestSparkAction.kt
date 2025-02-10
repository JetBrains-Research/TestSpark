package org.jetbrains.research.testspark.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import org.jetbrains.research.testspark.actions.controllers.TestGenerationController
import org.jetbrains.research.testspark.actions.controllers.VisibilityController
import org.jetbrains.research.testspark.display.TestSparkDisplayManager
import org.jetbrains.research.testspark.langwrappers.PsiHelperProvider
import org.jetbrains.research.testspark.tools.TestsExecutionResultManager

/**
 * Represents an action to be performed in the TestSpark plugin.
 *
 * This class extends the AnAction class and is responsible for handling the action performed event.
 * It creates a dialog wrapper and displays it when the associated action is performed.
 */
class TestSparkAction : AnAction() {

    /**
     * Handles the action performed event.
     *
     * This method is called when the associated action is performed.
     *
     * @param e The AnActionEvent object representing the action event.
     *           It provides information about the event, such as the source of the event and the project context.
     *           This parameter is required.
     */
    override fun actionPerformed(e: AnActionEvent) {
        TestSparkActionWindow(
            e = e,
            visibilityController = VisibilityController(),
            testGenerationController = TestGenerationController(),
            testSparkDisplayManager = TestSparkDisplayManager(),
            testsExecutionResultManager = TestsExecutionResultManager(),
        )
    }

    /**
     * Updates the state of the action based on the provided event.
     *
     * @param e `AnActionEvent` object representing the event
     */
    override fun update(e: AnActionEvent) {
        val file = e.dataContext.getData(CommonDataKeys.PSI_FILE)

        if (file == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val psiHelper = PsiHelperProvider.getPsiHelper(file)
        e.presentation.isEnabledAndVisible = (psiHelper != null) && psiHelper.availableForGeneration(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
