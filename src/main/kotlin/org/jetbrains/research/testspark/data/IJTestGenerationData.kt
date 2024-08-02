package org.jetbrains.research.testspark.data

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiImportStatement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPackageStatement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.research.testspark.core.data.TestGenerationData
import java.io.File

/**
 * This will be the PSI based IR eventually. For now make it extend TestGenerationData for small behaviour-preserving refactors
 */
class IJTestGenerationData(

    val packageStatement: PsiPackageStatement?,
    val imports: List<PsiImportStatement>,
    val testMethods: List<PsiMethod>,
    val helperMethods: List<PsiMethod>,
    val helperFields: List<PsiField>,
    // TODO may need to add static initializers for kex
    testGenerationData: TestGenerationData,

) : TestGenerationData(
    testGenerationData.testGenerationResultList,
    testGenerationData.resultName,
    testGenerationData.fileUrl,
    testGenerationData.resultPath,
    testGenerationData.testResultName,
    testGenerationData.baseDir,
    testGenerationData.importsCode,
    testGenerationData.packageName,
    testGenerationData.runWith,
    testGenerationData.otherInfo,
    testGenerationData.polyDepthReducing,
    testGenerationData.inputParamsDepthReducing,
    testGenerationData.compilableTestCases,
) {
    companion object {
        /**
         * Factory method
         * @param code the test code passed as a string, to initialise the Psi fields
         * @param testGenerationData used to initialise super class fields
         * @param project for obtaining a Psi file from the code string
         */
        fun buildFromCodeString(
            code: String,
            testGenerationData: TestGenerationData,
            project: Project,
        ): IJTestGenerationData {
            // TODO set filetype according to the language
            val psiFile = project.createPsiFile(code, "GeneratedTests.java")
            return ApplicationManager.getApplication().runReadAction<IJTestGenerationData> {
                val (testMethods, helperMethods) = psiFile.partitionMethods()
                IJTestGenerationData(
                    PsiTreeUtil.findChildOfType(psiFile, PsiPackageStatement::class.java),
                    PsiTreeUtil.findChildrenOfType(psiFile, PsiImportStatement::class.java).toList(),
                    testMethods,
                    helperMethods,
                    PsiTreeUtil.findChildrenOfType(psiFile, PsiField::class.java).toList(),
                    testGenerationData,
                )
            }
        }

        /**
         * Partitioning by methods having @Test annotation
         * Works for multiple versions of Junit and even if the Junit import doesn't exist in the PSI file
         */
        private fun PsiFile.partitionMethods(): Pair<List<PsiMethod>, List<PsiMethod>> {
            return PsiTreeUtil.findChildrenOfType(this, PsiMethod::class.java)
                .partition { method -> method.annotations.any { it.text.contains("Test") } }
        }

        /**
         * Creates a PsiFile from a given string content.
         *
         * @param content The content to write to the PsiFile.
         * @param fileName The name of the file (used to determine the file type).
         * @return The PsiFile with the given content.
         */
        private fun Project.createPsiFile(content: String, fileName: String): PsiFile {
            // Create a temporary file
            val tempFile = File.createTempFile(fileName, null)
            tempFile.writeText(content)

            // Refresh the local file system to make IntelliJ aware of the new file
            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tempFile)!!
            val (document, psiFile) = ApplicationManager.getApplication().runReadAction<Pair<Document, PsiFile>> {
                FileDocumentManager.getInstance().getDocument(virtualFile)!! to
                    PsiManager.getInstance(this).findFile(virtualFile)!!
            }
            ApplicationManager.getApplication().invokeAndWait {
                FileDocumentManager.getInstance().saveDocument(document)
            }
            return psiFile
        }

        // TODO remove after the planned separation of the IR and metadata (file names and paths) in IJTestGenerationData
        fun nullInitializer(): IJTestGenerationData {
            return IJTestGenerationData(null, listOf(), listOf(), listOf(), listOf(), TestGenerationData())
        }
    }
}
