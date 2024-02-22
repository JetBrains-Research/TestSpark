package org.jetbrains.research.testspark.appstarter

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import kotlin.system.exitProcess

class TestSparkStarter: ApplicationStarter {
    @Deprecated("Specify it as `id` for extension definition in a plugin descriptor")
    override val commandName: String ="testspark"

    override fun main(args: List<String>) {

        // Project path
        val projectPath = args[1]
        // Path to the target file (.java file)
        val filePath = "$projectPath/${args[2]}"
        // CUT name (<package-name>.<class-name>)
        val classUnderTestName = args[3]
        // Paths to compilation output of the project under test (seperated by ':')
        val classPath = args[4]
        // Selected mode
        val model = args[5]
        // Token
        val token = args[6]

        // open project
        println("Test generation requested for $projectPath")
        val project = ProjectUtil.openOrImport(projectPath, null, true) ?: run {
            println("couldn't find project in $projectPath")
            exitProcess(1)
        }
        println("Detected project: ${project}")

//        open target file
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath) ?: run {
            println("couldn't open file $filePath")
            exitProcess(1)
        }

        // get target PsiClass
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as PsiJavaFile
        val targetPsiClass = detectPsiClass(psiFile.classes, classUnderTestName) ?: run {
            println("couldn't find $classUnderTestName in $filePath")
            exitProcess(1)
        }

        println("PsiClass ${targetPsiClass.qualifiedName} is detected")

        // ToDo integrate with current implementation of LLM-based test generation in TestSpark

    }

    private fun detectPsiClass(classes: Array<PsiClass>, classUnderTestName: String): PsiClass? {
        for (psiClass in classes){
            if(psiClass.qualifiedName == classUnderTestName){
                return psiClass
            }
        }
        return null
    }
}