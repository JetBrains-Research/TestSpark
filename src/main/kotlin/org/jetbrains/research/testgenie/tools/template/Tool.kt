package org.jetbrains.research.testgenie.tools.template

import com.intellij.openapi.actionSystem.AnActionEvent

interface Tool {
    val name: String
    fun generateTestsForClass(e: AnActionEvent)
    fun generateTestsForMethod(e: AnActionEvent)
    fun generateTestsForLine(e: AnActionEvent)
}
