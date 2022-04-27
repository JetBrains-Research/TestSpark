package com.github.mitchellolsthoorn.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages


class GenerateTestsForClassAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        Messages.showErrorDialog(
            e.project,
            "Generating tests for the class!" + e.dataContext.toString(),
            "Generate Tests for Class"
        )
    }

//    override fun update(e: AnActionEvent) {
//        val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
//    }
}