package org.jetbrains.research.testgenie.tools

import com.intellij.openapi.actionSystem.AnActionEvent

interface Tool {
    fun generateTestsForClass(e: AnActionEvent)
    fun generateTestsForMethod(e: AnActionEvent)
    fun generateTestsForLine(e: AnActionEvent)
}
