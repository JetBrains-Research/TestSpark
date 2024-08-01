package org.jetbrains.research.testspark.data

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.research.testspark.core.data.TestGenerationData

/**
 * This will be the PSI based IR eventually. For now make it extend TestGenerationData for small behaviour-preserving refactors
 */
class IJTestGenerationData(

    val packageStatement: PsiPackageStatement?,
    val imports: List<PsiImportStatement>,
    val testMethods: List<PsiMethod>,
    val helperMethods: List<PsiMethod>,
    val helperFields: List<PsiField>,
    //TODO may need to add static initializers for kex
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
    testGenerationData.compilableTestCases
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
            project: Project
        ): IJTestGenerationData {
            val psiFile = createPsiFileFromString(code, "GeneratedTests.java", project, Language.findInstance(
                JavaLanguage::class.java))
            val (testMethods, helperMethods) = psiFile.partitionMethods()
            return IJTestGenerationData(
                PsiTreeUtil.findChildOfType(psiFile, PsiPackageStatement::class.java),
//                PsiTreeUtil.findChildOfType(psiFile, PsiClass::class.java)!!,
                PsiTreeUtil.findChildrenOfType(psiFile, PsiImportStatement::class.java).toList(),
                testMethods,
                helperMethods,
                PsiTreeUtil.findChildrenOfType(psiFile, PsiField::class.java).toList(),
                testGenerationData,
            )
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
         * @param project The IntelliJ project context.
         * @param language The language of the file.
         * @return The PsiFile with the given content.
         */
        private fun createPsiFileFromString(content: String, fileName: String, project: Project, language: Language): PsiFile {
            // Create a LightVirtualFile with the given content
            val lightFile = LightVirtualFile(fileName, language, content)

            // Create a PsiFile from the LightVirtualFile
            return PsiManager.getInstance(project).findFile(lightFile)!!
        }
    }
}