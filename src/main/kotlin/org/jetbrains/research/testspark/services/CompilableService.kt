package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import java.io.File

class CompilableService(private val project: Project) {
    private val javaHomeDirectory = ProjectRootManager.getInstance(project).projectSdk!!.homeDirectory!!

    fun compileCode(path: String, projectBuildPath: String): Pair<Boolean, String> {
        // find the proper javac
        val javaCompile = File(javaHomeDirectory.path).walk().filter { it.name.equals("javac") && it.isFile }.first()
        // compile file
        val errorMsg = project.service<CommandLineService>().runCommandLine(
            arrayListOf(
                javaCompile.absolutePath,
                "-cp",
                project.service<CommandLineService>().getPath(projectBuildPath),
                path,
            ),
        )

        // create .class file path
        val classFilePath = path.replace(".java", ".class")

        // check is .class file exists
        return Pair(File(classFilePath).exists(), errorMsg)
    }
}
