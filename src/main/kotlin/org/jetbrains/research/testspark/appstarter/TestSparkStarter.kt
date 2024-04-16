package org.jetbrains.research.testspark.appstarter

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.services.ProjectContextService
import org.jetbrains.research.testspark.services.SettingsProjectService
import org.jetbrains.research.testspark.services.TestStorageProcessingService
import org.jetbrains.research.testspark.tools.ProjectUnderTestFileCreator
import org.jetbrains.research.testspark.tools.llm.Llm
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.generation.LLMProcessManager
import org.jetbrains.research.testspark.tools.llm.generation.PromptManager
import org.jetbrains.research.testspark.tools.llm.generation.RequestManager
import java.io.File
import kotlin.system.exitProcess


class TestSparkStarter : ApplicationStarter {
    @Deprecated("Specify it as `id` for extension definition in a plugin descriptor")
    override val commandName: String = "testspark"

    /** Sets main (start) thread for IDE in headless as not edt. */
    override val requiredModality: Int = ApplicationStarter.NOT_IN_EDT

    @Suppress("TooGenericExceptionCaught")
    @OptIn(ExperimentalSerializationApi::class)
    override fun main(args: List<String>) {

        // Project path
        val projectPath = args[1]
        // Path to the target file (.java file)
        val filePath = "$projectPath/${args[2]}"
        // CUT name (<package-name>.<class-name>)
        val classUnderTestName = args[3]
        // Paths to compilation output of the project under test (seperated by ':')
        val classPath = "$projectPath:${args[4]}"
        // Selected mode
        val model = args[5]
        // Token
        val token = args[6]
        // A txt file containing the prompt template
        val promptTemplateFile = args[7]
        // Output directory
        val output = args[8]


        /**
         * Set output directory in order to save the produced prompts and LLM responses during test generation.
         * **The solution of having a public static field is bad but fast.**
         */
        ProjectUnderTestFileCreator.projectUnderTestOutputDirectory = output

        ProjectUnderTestFileCreator.log("Test generation requested for $projectPath")
        ProjectUnderTestFileCreator.log("classPath: '$classPath'")
        // println("Test generation requested for $projectPath")
        // println("classPath: '$classPath'")

        ApplicationManager.getApplication().invokeAndWait {
            val project = ProjectUtil.openOrImport(projectPath, null, true) ?: run {
                ProjectUnderTestFileCreator.log("Could not find project in '$projectPath'")
                // println("Could not find project in '$projectPath'")
                exitProcess(1)
            }
            ProjectUnderTestFileCreator.log("Detected project: $project")
            // println("Detected project: $project")
            // Continue when the project is indexed
            ProjectUnderTestFileCreator.log("Indexing project...")
            // println("Indexing project...")
            project.let {
                DumbService.getInstance(it).runWhenSmart {
                    // open target file
                    val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath) ?: run {
                        ProjectUnderTestFileCreator.log("Could not open file '$filePath'")
                        // println("couldn't open file '$filePath'")
                        exitProcess(1)
                    }

                    // get target PsiClass
                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as PsiJavaFile
                    val targetPsiClass = detectPsiClass(psiFile.classes, classUnderTestName) ?: run {
                        ProjectUnderTestFileCreator.log("Could not find '$classUnderTestName' in '$filePath'")
                        // println("Could not find '$classUnderTestName' in '$filePath'")
                        exitProcess(1)
                    }

                    ProjectUnderTestFileCreator.log(
                        "PsiClass '${targetPsiClass.qualifiedName}' is detected! Start the test generation process.")
                    // println("PsiClass '${targetPsiClass.qualifiedName}' is detected! Start the test generation process.")

                    // update settings
                    project.service<ProjectContextService>().projectClassPath = classPath
                    project.service<SettingsProjectService>().state.buildPath = classPath
                    SettingsArguments.settingsState?.currentLLMPlatformName =
                        TestSparkDefaultsBundle.defaultValue("grazie")
                    SettingsArguments.settingsState!!.llmPlatforms[1].token = token
                    SettingsArguments.settingsState!!.llmPlatforms[1].model = model
                    SettingsArguments.settingsState!!.classPrompt = File(promptTemplateFile).readText()
                    project.service<ProjectContextService>().resultPath = output
                    project.service<ProjectContextService>().classFQN = targetPsiClass.qualifiedName
                    project.service<ProjectContextService>().fileUrl = output
                    project.service<ProjectContextService>().cutPsiClass = targetPsiClass
                    project.service<ProjectContextService>().cutModule = ProjectFileIndex.getInstance(project)
                        .getModuleForFile(project.service<ProjectContextService>().cutPsiClass!!.containingFile.virtualFile)!!
                    //        CompilerModuleExtension.getInstance(project.service<ProjectContextService>().cutModule!!)?.compilerOutputPath = psiFile.virtualFile

                    ProjectUnderTestFileCreator.log("Indexing is done")
                    // println("Indexing is done")

                    // get target classes
                    val classesToTest = Llm().getClassesUnderTest(project, targetPsiClass)
                    ProjectUnderTestFileCreator.log("Detected CUTs: classesToTest=$classesToTest")
                    // println("Detected CUTs: classesToTest=$classesToTest")

                    // get package name
                    val packageList = targetPsiClass.qualifiedName.toString().split(".").toMutableList()
                    packageList.removeLast()

                    val packageName = packageList.joinToString(".")

                    val llmProcessManager = LLMProcessManager(
                        project,
                        PromptManager(project, targetPsiClass, classesToTest)
                    )

                    ProjectUnderTestFileCreator.log("Starting test generation with packageName='$packageName'")
                    // println("Starting test generation with packageName='$packageName'")
                    llmProcessManager.runTestGenerator(
                        indicator = null,
                        FragmentToTestData(CodeType.CLASS),
                        packageName
                    )
                    ProjectUnderTestFileCreator.log("Test generation for packageName='$packageName' has finished")
                    // println("Test generation for packageName='$packageName' has finished")

                    // Run test file
                    ProjectUnderTestFileCreator.log("Running generated tests and collecting Jacoco coverage...")
                    // println("Running generated tests and collecting Jacoco coverage...")
                    runTestsAndCollectJacocoCoverage(project, output, packageList, classPath)
                    ProjectUnderTestFileCreator.log("Run of the generated tests & coverage collection have finished")
                    // println("Run of the generated tests & coverage collection have finished")

                    ProjectManager.getInstance().closeAndDispose(project)

                    ProjectUnderTestFileCreator.log("[TestSpark Starter] Exiting the headless mode")
                    // println("[TestSpark Starter] Exiting the headless mode")
                    exitProcess(0)
                }
            }
        }
    }

    private fun runTestsAndCollectJacocoCoverage(
        project: Project, out: String, packageList: MutableList<String>, classPath: String) {
        val targetDirectory = "$out${File.separator}${packageList.joinToString(File.separator)}"
        ProjectUnderTestFileCreator.log("Collecting coverage for tests in '$targetDirectory")
        // println("Run tests in '$targetDirectory")

        File(targetDirectory).walk().forEach {
            ProjectUnderTestFileCreator.log("Considering file '${it.name}'")
            // println("Considering file '${it.name}'")

            if (it.name.endsWith(".class")) {
                ProjectUnderTestFileCreator.log("Collecting coverage for test '${it.name}'")
                // println("Running test '${it.name}'")

                // TODO: test case may start with un uppercase letter!
                var testcaseName = it.nameWithoutExtension.removePrefix("Generated")
                testcaseName = testcaseName[0].lowercaseChar() + testcaseName.substring(1)

                // Test is compiled and it is ready to run jacoco
                val testExecutionError = project.service<TestStorageProcessingService>().createXmlFromJacoco(
                    it.nameWithoutExtension,
                    "$targetDirectory${File.separator}jacoco-${it.nameWithoutExtension}",
                    testcaseName,
                    classPath,
                    packageList.joinToString("."),
                    out,
                )

                saveException(testcaseName, targetDirectory,  testExecutionError)
            }
        }
    }

    private fun saveException(testcaseName: String,
                              targetDirectory: String,
                              testExecutionError: String) {
        if (testExecutionError.isBlank() || !testExecutionError.contains("Exception", ignoreCase = false))
            return
        val targetPath = "$targetDirectory/$testcaseName-exception.log"

        // Save the exception
        File(targetPath).writeText(testExecutionError.replace("\tat ", "\nat "))
    }


    private fun detectPsiClass(classes: Array<PsiClass>, classUnderTestName: String): PsiClass? {
        for (psiClass in classes) {
            if (psiClass.qualifiedName == classUnderTestName) {
                return psiClass
            }
        }
        return null
    }

//
//    fun isMavenProject(projectPath: String): Boolean {
//        val pom = File("$projectPath/pom.xml")
//        return pom.exists()
//    }

//    fun refreshMaven(directoryPath: String) {
//        val processBuilder = ProcessBuilder()
//        processBuilder.command("mvn", "clean", "install")
//        processBuilder.directory(File(directoryPath))
//        try {
//            val process = processBuilder.start()
//            process.waitFor()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//    }
}