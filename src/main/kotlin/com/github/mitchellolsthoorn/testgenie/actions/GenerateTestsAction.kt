package com.github.mitchellolsthoorn.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class GenerateTestsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showErrorDialog(e.project, "Have you tried writing some tests?", "Write Some Tests")
    }
}