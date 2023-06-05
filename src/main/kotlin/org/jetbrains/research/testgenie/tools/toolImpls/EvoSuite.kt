package org.jetbrains.research.testgenie.tools.toolImpls

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.research.testgenie.actions.calculateLinesToInvalidate
import org.jetbrains.research.testgenie.actions.createEvoSuitePipeline
import org.jetbrains.research.testgenie.actions.getSurroundingLine
import org.jetbrains.research.testgenie.actions.getSurroundingMethod
import org.jetbrains.research.testgenie.helpers.generateMethodDescriptor
import org.jetbrains.research.testgenie.services.RunnerService
import org.jetbrains.research.testgenie.tools.Tool
import org.jetbrains.research.testgenie.tools.evosuite.Pipeline

class EvoSuite : Tool {
    override fun generateTestsForClass(e: AnActionEvent) {
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        val project = e.project ?: return
        val runnerService = project.service<RunnerService>()
        if (!runnerService.verify(psiFile)) return
        val linesToInvalidateFromCache = calculateLinesToInvalidate(psiFile)
        val evoSuitePipeline: Pipeline = createEvoSuitePipeline(e) ?: return
        evoSuitePipeline.forClass().invalidateCache(linesToInvalidateFromCache).runTestGeneration()
    }

    override fun generateTestsForMethod(e: AnActionEvent) {
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val project = e.project ?: return
        val runnerService = project.service<RunnerService>()
        if (!runnerService.verify(psiFile)) return
        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret) ?: return
        val methodDescriptor = generateMethodDescriptor(psiMethod)
        val doc: Document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return
        val cacheStartLine: Int = doc.getLineNumber(psiMethod.startOffset)
        val cacheEndLine: Int = doc.getLineNumber(psiMethod.endOffset)
        val linesToInvalidateFromCache = calculateLinesToInvalidate(psiFile)
        val evoSuitePipeline: Pipeline = createEvoSuitePipeline(e) ?: return
        evoSuitePipeline
            .forMethod(methodDescriptor)
            .withCacheLines(cacheStartLine, cacheEndLine)
            .invalidateCache(linesToInvalidateFromCache)
            .runTestGeneration()
    }

    override fun generateTestsForLine(e: AnActionEvent) {
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val selectedLine: Int = getSurroundingLine(psiFile, caret)?.plus(1)
            ?: return // lines in the editor and in EvoSuite are one-based
        val project = e.project ?: return
        val runnerService = project.service<RunnerService>()
        if (!runnerService.verify(psiFile)) return
        val linesToInvalidateFromCache = calculateLinesToInvalidate(psiFile)
        val evoSuitePipeline: Pipeline = createEvoSuitePipeline(e) ?: return
        evoSuitePipeline
            .forLine(selectedLine)
            .withCacheLines(selectedLine - 1, selectedLine - 1)
            .invalidateCache(linesToInvalidateFromCache)
            .runTestGeneration()
    }
}
