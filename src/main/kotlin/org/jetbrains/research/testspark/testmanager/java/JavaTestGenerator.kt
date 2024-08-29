package org.jetbrains.research.testspark.testmanager.java

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.testmanager.template.TestGenerator
import java.io.File

object JavaTestGenerator : TestGenerator {

    private val log = Logger.getInstance(this::class.java)

    override fun generateCode(
        project: Project,
        className: String,
        body: String,
        imports: Set<String>,
        packageString: String,
        runWith: String,
        otherInfo: String,
        testGenerationData: TestGenerationData,
    ): String {
        var testFullText = printUpperPart(className, imports, packageString, runWith, otherInfo)

        // Add each test (exclude expected exception)
        testFullText += body

        // close the test class
        testFullText += "}"

        testFullText.replace("\r\n", "\n")

        /**
         * for better readability and make the tests shorter, we reduce the number of line breaks:
         *  when we have three or more sequential \n, reduce it to two.
         */
        return formatCode(project, Regex("\n\n\n(?:\n)*").replace(testFullText, "\n\n"), testGenerationData)
    }

    override fun formatCode(project: Project, code: String, generatedTestData: TestGenerationData): String {
        var result = ""
        WriteCommandAction.runWriteCommandAction(project) {
            val fileName = generatedTestData.resultPath + File.separatorChar + "Formatted.java"
            // create a temporary PsiFile
            val psiFile: PsiFile = PsiFileFactory.getInstance(project)
                .createFileFromText(
                    fileName,
                    JavaLanguage.INSTANCE,
                    code,
                )

            CodeStyleManager.getInstance(project).reformat(psiFile)

            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
            result = document?.text ?: code

            File(fileName).delete()
        }

        return result
    }

    private fun printUpperPart(
        className: String,
        imports: Set<String>,
        packageString: String,
        runWith: String,
        otherInfo: String,
    ): String {
        var testText = ""

        // Add package
        if (packageString.isNotBlank()) {
            testText += "package $packageString;\n"
        }

        // add imports
        imports.forEach { importedElement ->
            testText += "$importedElement\n"
        }

        testText += "\n"

        // add runWith if exists
        if (runWith.isNotBlank()) {
            testText += "@RunWith($runWith)\n"
        }
        // open the test class
        testText += "public class $className {\n\n"

        // Add other presets (annotations, non-test functions)
        if (otherInfo.isNotBlank()) {
            testText += otherInfo
        }

        return testText
    }
}
