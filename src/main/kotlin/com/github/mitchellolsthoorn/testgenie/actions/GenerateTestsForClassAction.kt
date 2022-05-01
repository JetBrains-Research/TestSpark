package com.github.mitchellolsthoorn.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages

/**
 * This class is responsible for generating tests for a selected class.
 */
class GenerateTestsForClassAction : AnAction() {

    /**
     * Performs the main functionality of the action by determining the selected class and generating tests.
     */
    override fun actionPerformed(e: AnActionEvent) {
        val editor : Editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val selectedText : String? = editor.caretModel.currentCaret.selectedText

        // TODO: actually determine the class and generate tests for it
        Messages.showInfoMessage(
            e.project,
            "Generating tests for the class `${e.dataContext}`\nThe selected text is:\n$selectedText",
            "Generate Tests for Class"
        )
    }

    /**
     * Restricts the action so that it is only enabled when a user actually selects a class.
     */
    override fun update(e: AnActionEvent) {
        val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel : CaretModel = editor.caretModel

        // TODO: actually check if a class has been selected
        e.presentation.isEnabledAndVisible = caretModel.currentCaret.hasSelection()
    }
}