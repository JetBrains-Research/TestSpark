package org.jetbrains.research.testgenie.tools.llm

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.PsiClass
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.actions.getClassDisplayName
import org.jetbrains.research.testgenie.actions.getClassFullText
import org.jetbrains.research.testgenie.actions.getSignatureString
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.services.TestCaseDisplayService
import org.jetbrains.research.testgenie.tools.ProjectBuilder
import org.jetbrains.research.testgenie.tools.llm.generation.LLMProcessManager
import java.io.File
import java.util.*

private var prompt = ""

class Pipeline(
    private val project: Project,
    projectClassPath: String,
    private val interestingPsiClasses: Set<PsiClass>,
    private val cut: PsiClass,
    private val packageName: String,
    private val polymorphismRelations: MutableMap<PsiClass, MutableList<PsiClass>>,
    modTs: Long,
    private val fileUrl: String,
    private val classFQN: String,
) {

    private val log = Logger.getInstance(this::class.java)

    private val sep = File.separatorChar

    private val id = UUID.randomUUID().toString()
    private val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"
    private val testResultName = "test_gen_result_$id"

    private val resultPath = "$testResultDirectory$testResultName"

    // TODO move all interactions with Workspace to Manager
    var key = Workspace.TestJobInfo(fileUrl, classFQN, modTs, testResultName, projectClassPath)

    private val processManager =
        LLMProcessManager(project, projectClassPath)

    // TODO("Removed unused input parameters. needs o be refactored after finalizing the implementation")

    fun forClass(): Pipeline {
        prompt = generatePrompt()
        return this
    }

    private fun generatePrompt(): String {
        // prompt: start the request
        var prompt =
            "Generate unit tests in Java for ${getClassDisplayName(cut)} to achieve 100% line coverage for this class.\nDont use @Before and @After test methods.\nMake tests as atomic as possible.\nAll tests should be for JUnit 4.\nIn case of mocking, use Mockito 5. But, do not use mocking for all tests.\n"

        // prompt: source code
        prompt += "The source code of class under test is as follows:\n```\n${getClassFullText(cut)}\n```\n"

        // prompt: signature of methods in the classes used by CUT
        prompt += "Here are the method signatures of classes used by the class under test. Only use these signatures for creating objects, not your own ideas.\n"
        for (interestingPsiClass: PsiClass in interestingPsiClasses) {
            if (interestingPsiClass.qualifiedName!!.startsWith("java")) {
                continue
            }
            val interestingPsiClassQN = interestingPsiClass.qualifiedName
            if (interestingPsiClassQN.equals(cut.qualifiedName)) {
                continue
            }

            prompt += "=== methods in ${getClassDisplayName(interestingPsiClass)}:\n"
            for (currentPsiMethod in interestingPsiClass.allMethods) {
                // Skip java methods
                if (currentPsiMethod.containingClass!!.qualifiedName!!.startsWith("java")) {
                    continue
                }
                prompt += " - ${currentPsiMethod.getSignatureString()}\n"
            }
            prompt += "\n\n"
        }

        // prompt: add polymorphism relations between involved classes
        prompt += "=== polymorphism relations:\n"
        polymorphismRelations.forEach { entry ->
            for (currentSubClass in entry.value) {
                prompt += "${currentSubClass.qualifiedName} is a sub-class of ${entry.key.qualifiedName}.\n"
            }
        }
        // Make sure that LLM does not provide extra information other than the test file
        prompt += "put the generated test between ```"

        return prompt
    }

    fun runTestGeneration() {
        // TODO move all interactions with Workspace to Manager
        val workspace = project.service<Workspace>()
        workspace.addPendingResult(testResultName, key)

        // TODO move all interactions with TestCaseDisplayService to Manager
        project.service<TestCaseDisplayService>().resultName = testResultName

        val projectBuilder = ProjectBuilder(project)

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestGenieBundle.message("testGenerationMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    if (indicator.isCanceled) {
                        indicator.stop()
                        return
                    }

                    if (projectBuilder.runBuild(indicator)) {
                        processManager.runLLMTestGenerator(indicator, prompt, resultPath, packageName, cut, classFQN)
                    }
                }
            })

        project.service<TestCaseDisplayService>().fileUrl = fileUrl
    }
}
