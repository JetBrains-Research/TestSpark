package org.jetbrains.research.testgenie.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.Util
import org.jetbrains.research.testgenie.actions.getSurroundingClass
import org.jetbrains.research.testgenie.data.CodeTypeAndAdditionData
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.tools.template.generation.ProcessManager
import java.io.File
import java.util.UUID

class Pipeline(
    e: AnActionEvent,
    private val packageName: String,
) {
    private val project = e.project!!

    private val sep = File.separatorChar

    private val projectClassPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path

    private val log = Logger.getInstance(this::class.java)

    private val id = UUID.randomUUID().toString()
    private val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"
    private val testResultName = "test_gen_result_$id"
    private var baseDir = "$testResultDirectory$testResultName-validation"

    private val serializeResultPath = "\"$testResultDirectory$testResultName\""

    private val resultPath = "$testResultDirectory$testResultName"

    private val vFile = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)!!
    private val fileUrl = vFile.presentableUrl
    private val modificationStamp = vFile.modificationStamp

    private val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
    private val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
    private val cutPsiClass: PsiClass = getSurroundingClass(psiFile, caret)
    private val cutModule: Module = ProjectFileIndex.getInstance(project).getModuleForFile(cutPsiClass.containingFile.virtualFile)!!

    private val classFQN = cutPsiClass.qualifiedName!!

    init {
        Util.makeTmp()
        Util.makeDir(baseDir)

        project.service<Workspace>().key = getKey(fileUrl, classFQN, modificationStamp, testResultName, projectClassPath)
    }

    /**
     * Builds the project and launches generation on a separate thread.
     */
    fun runTestGeneration(processManager: ProcessManager, codeType: CodeTypeAndAdditionData) {
        clearDataBeforeTestGeneration(project, testResultName)

        val projectBuilder = ProjectBuilder(project)

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestGenieBundle.message("testGenerationMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    if (indicatorIsCanceled(project, indicator)) return

                    if (projectBuilder.runBuild(indicator)) {
                        if (indicatorIsCanceled(project, indicator)) return

                        processManager.runTestGenerator(
                            indicator,
                            codeType,
                            projectClassPath,
                            resultPath,
                            serializeResultPath,
                            packageName,
                            cutModule,
                            classFQN,
                            fileUrl,
                            testResultName,
                            baseDir,
                            log,
                            modificationStamp,
                        )
                    }

                    if (indicatorIsCanceled(project, indicator)) return

                    indicator.stop()
                }
            })
    }
}
