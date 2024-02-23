package org.jetbrains.research.testspark.appstarter

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.services.ProjectContextService
import org.jetbrains.research.testspark.services.SettingsProjectService
import org.jetbrains.research.testspark.tools.llm.Llm
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.generation.LLMProcessManager
import org.jetbrains.research.testspark.tools.llm.generation.PromptManager
import java.io.File
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
        val classPath = "$projectPath/${args[4]}"
        // Selected mode
        val model = args[5]
        // Token
        val token = args[6]
        // A txt file containing the prompt template
        val promptTemplateFile = args[7]
        // Output directory
        val output = args[8]

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

        println("PsiClass ${targetPsiClass.qualifiedName} is detected! Start the test generation process.")

        // update settings
        project.service<ProjectContextService>().projectClassPath = classPath
        project.service<SettingsProjectService>().state.buildPath = classPath
        SettingsArguments.settingsState?.currentLLMPlatformName = TestSparkDefaultsBundle.defaultValue("grazie")
        SettingsArguments.settingsState!!.llmPlatforms[1].token = token
        SettingsArguments.settingsState!!.llmPlatforms[1].model = model
        SettingsArguments.settingsState!!.classPrompt = File(promptTemplateFile).readText()
        project.service<ProjectContextService>().resultPath = output
        project.service<ProjectContextService>().classFQN = targetPsiClass.qualifiedName
        project.service<ProjectContextService>().fileUrl = output
        // TODO project.service<ProjectContextService>().fileUrl

        // Continue when the project is indexed
        println("Indexing project...")
        DumbService.getInstance(project).runWhenSmart{
            println("Indexing is done")
            // get target classes
            val classesToTest =  Llm().getClassesUnderTest(project, targetPsiClass)

            println("Detected CUTs: $classesToTest")

            // get package name
            val packageList = targetPsiClass.qualifiedName.toString().split(".").toMutableList()
            packageList.removeLast()

            val packageName = packageList.joinToString(".")

            val llmProcessManager = LLMProcessManager(
                project,
                PromptManager(project,targetPsiClass,classesToTest)
            )

            llmProcessManager.runTestGenerator(indicator=null,
                FragmentToTestData(CodeType.CLASS),
                packageName
            )

            // ToDo add test generation with Jacoco for tests in out variable
        }





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