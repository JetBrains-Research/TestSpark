package com.github.mitchellolsthoorn.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.evosuite.EvoSuite


class GenerateTestsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.project ?: return

        val project: Project = e.project!!

        val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path + "/target/classes/"

        val evo = EvoSuite()
        val file = e.dataContext.getData(CommonDataKeys.PSI_FILE)
        file ?: return

        val command = arrayOf(
            "-generateSuite",
            "-class",  "demo.ShitClass",
            "-projectCP", projectPath,
            "-Dtest_comments", "true",
//            "-Dassertions", "false",
            "-Dserialize_ga", "true"
        )

        val result: Any = evo.parseCommandLine(command)

    }
}